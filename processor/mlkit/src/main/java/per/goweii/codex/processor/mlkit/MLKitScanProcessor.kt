package per.goweii.codex.processor.mlkit

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.mlkit.internal.toBarcodeFormat
import per.goweii.codex.processor.mlkit.internal.toCodeResult

class MLKitScanProcessor(
    private val formats: Array<CodeFormat> = arrayOf(CodeFormat.QR_CODE)
) : DecodeProcessor<ImageProxy> {
    private val scanner: BarcodeScanner by lazy {
        val formats = formats.map { it.toBarcodeFormat() }
        if (formats.isEmpty()) {
            throw RuntimeException("Formats cannot be empty")
        }
        val format0 = formats[0]
        val format1 = mutableListOf<Int>()
        for (i in 1 until formats.size) {
            format1.add(formats[i])
        }
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(format0, *format1.toIntArray())
            .build()
        BarcodeScanning.getClient(options)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun process(
        input: ImageProxy,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = input.image ?: run {
            onFailure.invoke(CodeNotFoundException)
            input.close()
            return
        }
        scanner.process(InputImage.fromMediaImage(image, 0))
            .addOnSuccessListener { result ->
                val results = if (result.isNotEmpty()) {
                    result.filter {
                        !it.rawValue.isNullOrEmpty()
                    }.map {
                        it.toCodeResult()
                    }
                } else null
                if (results.isNullOrEmpty()) {
                    onFailure.invoke(CodeNotFoundException)
                } else {
                    onSuccess.invoke(results)
                }
            }.addOnFailureListener { e: Exception ->
                onFailure.invoke(e)
            }
            .addOnCompleteListener {
                input.close()
            }
    }
}

package per.goweii.codex.processor.hms

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import androidx.core.util.forEach
import com.huawei.hms.ml.scan.HmsScanAnalyzer
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.huawei.hms.mlsdk.common.MLFrame
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.hms.internal.toCodeResult
import per.goweii.codex.processor.hms.internal.toScanType

class HmsScanProcessor(
    private val formats: Array<CodeFormat> = arrayOf(CodeFormat.QR_CODE)
) : DecodeProcessor<ImageProxy> {
    private val notFountException = CodeNotFoundException
    private val options by lazy {
        HmsScanAnalyzerOptions.Creator().apply {
            val formats = formats.map { it.toScanType() }
            if (formats.isEmpty()) {
                throw RuntimeException("Formats cannot be empty")
            }
            val format0 = formats[0]
            val format1 = mutableListOf<Int>()
            for (i in 1 until formats.size) {
                format1.add(formats[i])
            }
            setHmsScanTypes(format0, *format1.toIntArray())
        }.create()
    }
    private val analyzer by lazy {
        HmsScanAnalyzer(options)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun process(
        input: ImageProxy,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = MLFrame.fromMediaImage(input.image, 0)
        val results = analyzer.analyseFrame(image)
        val codeResults = arrayListOf<CodeResult>()
        results.forEach { _, value ->
            if (value.originalValue.isNotEmpty()) {
                val result = value.toCodeResult()
                result.rotate270(input.width.toFloat(), input.height.toFloat())
                codeResults.add(result)
            }
        }
        if (codeResults.isEmpty()) {
            onFailure.invoke(notFountException)
        } else {
            onSuccess.invoke(codeResults)
        }
    }
}
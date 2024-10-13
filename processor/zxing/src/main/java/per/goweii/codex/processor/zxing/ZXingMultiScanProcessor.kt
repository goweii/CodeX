package per.goweii.codex.processor.zxing

import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.zxing.internal.toBarcodeFormat
import per.goweii.codex.processor.zxing.internal.toCodeResult
import per.goweii.codex.scanner.ImageConverter
import java.util.concurrent.atomic.AtomicReference

class ZXingMultiScanProcessor(
    private val formats: Array<CodeFormat> = arrayOf(CodeFormat.QR_CODE)
) : DecodeProcessor<ImageProxy> {
    private val reader by lazy {
        MultiFormatReader()
    }
    private val multiReader by lazy {
        GenericMultipleBarcodeReader(reader)
    }
    private val hints = mutableMapOf<DecodeHintType, Any>().apply {
        put(DecodeHintType.CHARACTER_SET, "UTF-8")
        put(DecodeHintType.POSSIBLE_FORMATS, formats.map { it.toBarcodeFormat() })
    }
    private val reuseBuffer = AtomicReference<ByteArray?>(null)

    override fun process(
        input: ImageProxy,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val width = input.width
        val height = input.height
        val buffer = ImageConverter.imageToYByteArray(input, reuseBuffer.get())
        val source = PlanarYUVLuminanceSource(buffer, width, height, 0, 0, width, height, false)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val results = multiReader.decodeMultiple(binaryBitmap, hints)
            val codeResults = if (results.isNotEmpty()) {
                results.filter {
                    !it.text.isNullOrEmpty()
                }.map {
                    it.toCodeResult()
                }
            } else null
            if (codeResults.isNullOrEmpty()) {
                throw CodeNotFoundException
            }
            onSuccess.invoke(codeResults)
        } catch (e: Throwable) {
            reuseBuffer.set(buffer)
            onFailure.invoke(e)
        } finally {
            reader.reset()
        }
    }
}
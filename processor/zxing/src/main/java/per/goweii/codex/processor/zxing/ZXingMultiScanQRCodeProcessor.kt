package per.goweii.codex.processor.zxing

import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.zxing.internal.toCodeResult
import per.goweii.codex.scanner.ImageConverter
import java.util.concurrent.atomic.AtomicReference

class ZXingMultiScanQRCodeProcessor : DecodeProcessor<ImageProxy> {
    private val reader by lazy { QRCodeMultiReader() }
    private val hints by lazy {
        mutableMapOf<DecodeHintType, Any>().apply {
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
    }
    private val reuseBuffer = AtomicReference<ByteArray?>(null)

    override fun process(
        input: ImageProxy,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val width = input.width
        val height = input.height
        val buffer = ImageConverter.imageToYByteArray(input, reuseBuffer.get())
        val source = PlanarYUVLuminanceSource(buffer, width, height, 0, 0, width, height, false)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val results = reader.decodeMultiple(binaryBitmap, hints)
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
        } catch (e: Exception) {
            reuseBuffer.set(buffer)
            onFailure.invoke(e)
        } finally {
            reader.reset()
        }
    }
}
package per.goweii.codex.processor.zxing

import android.graphics.Bitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.zxing.internal.toCodeResult

class ZXingMultiDecodeQRCodeProcessor : DecodeProcessor<Bitmap> {
    private val notFountException = CodeNotFoundException
    private val reader by lazy { QRCodeMultiReader() }
    private val hints by lazy {
        mutableMapOf<DecodeHintType, Any>().apply {
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
    }

    override fun process(
        input: Bitmap,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val width = input.width
        val height = input.height
        val pixels = IntArray(width * height)
        input.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
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
                onFailure.invoke(notFountException)
            } else {
                onSuccess.invoke(codeResults)
            }
        } catch (e: Exception) {
            onFailure.invoke(e)
        } finally {
            reader.reset()
        }
    }
}
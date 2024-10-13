package per.goweii.codex.processor.zxing

import android.graphics.Bitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.zxing.internal.toBarcodeFormat
import per.goweii.codex.processor.zxing.internal.toCodeResult

class ZXingDecodeProcessor(
    private val formats: Array<CodeFormat> = arrayOf(CodeFormat.QR_CODE)
) : DecodeProcessor<Bitmap> {
    private val reader by lazy {
        MultiFormatReader().apply {
            setHints(mutableMapOf<DecodeHintType, Any>().apply {
                put(DecodeHintType.CHARACTER_SET, "UTF-8")
                put(DecodeHintType.POSSIBLE_FORMATS, formats.map { it.toBarcodeFormat() })
            })
        }
    }

    override fun process(
        input: Bitmap,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val width = input.width
        val height = input.height
        val pixels = IntArray(width * height)
        input.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result = reader.decodeWithState(binaryBitmap)
            if (!result.text.isNullOrEmpty()) {
                val codeResult = result.toCodeResult()
                onSuccess.invoke(listOf(codeResult))
            } else {
                onFailure.invoke(CodeNotFoundException)
            }
        } catch (e: Throwable) {
            onFailure.invoke(e)
        } finally {
            reader.reset()
        }
    }
}
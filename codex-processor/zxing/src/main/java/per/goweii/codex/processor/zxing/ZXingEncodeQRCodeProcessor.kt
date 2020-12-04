package per.goweii.codex.processor.zxing

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import per.goweii.codex.CodeErrorCorrectionLevel
import per.goweii.codex.encoder.EncodeProcessor
import per.goweii.codex.processor.zxing.internal.toErrorCorrectionLevel

class ZXingEncodeQRCodeProcessor(
    private val minSize: Int = 200,
    private val foregroundColor: Int = Color.BLACK,
    private val backgroundColor: Int = Color.WHITE,
    private val errorCorrectionLevel: CodeErrorCorrectionLevel = CodeErrorCorrectionLevel.M,
) : EncodeProcessor {
    private val writer by lazy { MultiFormatWriter() }

    private val hints
        get() = mutableMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 0)
            put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel.toErrorCorrectionLevel())
        }

    override fun process(
        input: String,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val bitMatrix = writer.encode(
                input,
                BarcodeFormat.QR_CODE,
                0,
                0,
                hints
            )
            onSuccess.invoke(bitMatrix.toBitmap())
        } catch (e: Exception) {
            onFailure.invoke(e)
        }
    }

    private fun BitMatrix.toBitmap(): Bitmap {
        var multiple = (minSize / width)
        if (minSize % width != 0) multiple++
        val realSize = multiple * width
        val bitmap = Bitmap.createBitmap(realSize, realSize, Bitmap.Config.RGB_565)
        fun Bitmap.setPixel(x: Int, y: Int, multiple: Int, color: Int) {
            for (mx in 0 until multiple) {
                val bx = multiple * x + mx
                for (my in 0 until multiple) {
                    val by = multiple * y + my
                    setPixel(bx, by, color)
                }
            }
        }
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (get(x, y)) foregroundColor else backgroundColor
                bitmap.setPixel(x, y, multiple, color)
            }
        }
        return bitmap
    }
}
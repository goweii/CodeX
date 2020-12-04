package per.goweii.codex.processor.zxing

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import per.goweii.codex.CodeErrorCorrectionLevel
import per.goweii.codex.CodeFormat
import per.goweii.codex.encoder.EncodeProcessor
import per.goweii.codex.processor.zxing.internal.toBarcodeFormat
import per.goweii.codex.processor.zxing.internal.toErrorCorrectionLevel

class ZXingEncodeProcessor(
    private val format: CodeFormat = CodeFormat.QR_CODE,
    private val errorCorrectionLevel: CodeErrorCorrectionLevel = CodeErrorCorrectionLevel.M,
    private val width: Int = 200,
    private val height: Int = 200
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
                format.toBarcodeFormat(),
                width,
                height,
                hints
            )
            onSuccess.invoke(bitMatrix.toBitmap())
        } catch (e: Exception) {
            onFailure.invoke(e)
        }
    }

    private fun BitMatrix.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (get(x, y)) {
                    bitmap.setPixel(x, y, Color.BLACK)
                } else {
                    bitmap.setPixel(x, y, Color.WHITE)
                }
            }
        }
        return bitmap
    }
}
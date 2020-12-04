package per.goweii.codex.processor.hms.plus

import android.graphics.Bitmap
import android.graphics.Color
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import per.goweii.codex.CodeErrorCorrectionLevel
import per.goweii.codex.CodeFormat
import per.goweii.codex.encoder.EncodeProcessor
import per.goweii.codex.processor.hms.plus.internal.toErrorCorrectionLevel
import per.goweii.codex.processor.hms.plus.internal.toScanType

class HmsPlusEncodeProcessor(
    private val format: CodeFormat = CodeFormat.QR_CODE,
    private val errorCorrectionLevel: CodeErrorCorrectionLevel = CodeErrorCorrectionLevel.M,
    private val width: Int = 200,
    private val height: Int = 200
) : EncodeProcessor {
    private val options by lazy {
        HmsBuildBitmapOption.Creator().apply {
            setBitmapBackgroundColor(Color.WHITE)
            setBitmapColor(Color.BLACK)
            setBitmapMargin(0)
            setQRErrorCorrection(errorCorrectionLevel.toErrorCorrectionLevel())
        }.create()
    }

    override fun process(
        input: String,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val bitmap = ScanUtil.buildBitmap(input, format.toScanType(), width, height, options)
            onSuccess.invoke(bitmap)
        } catch (e: Exception) {
            onFailure.invoke(e)
        }
    }
}
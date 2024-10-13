package per.goweii.codex.processor.wechat

import android.content.Context
import android.graphics.Bitmap
import org.opencv.core.Mat
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.Point
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.wechat.internal.WeChatQRCodeDetector

class WeChatQRCodeDecodeProcessor(context: Context) : DecodeProcessor<Bitmap> {
    private val detector = WeChatQRCodeDetector(context)

    override fun process(
        input: Bitmap,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        try {
            val points = arrayListOf<Mat>()
            val results = detector.detectAndDecode(input, points)

            if (results.isEmpty()) {
                onFailure.invoke(CodeNotFoundException)
                return
            }

            val size = results.size
            val codeResults = ArrayList<CodeResult>(size)
            for (i in 0 until size) {
                val string = results[i]
                val point = points[i]
                val corners = arrayOf(
                    Point(x = point[0, 0][0].toFloat(), y = point[0, 1][0].toFloat()),
                    Point(x = point[0, 0][0].toFloat(), y = point[0, 1][0].toFloat()),
                    Point(x = point[0, 0][0].toFloat(), y = point[0, 1][0].toFloat()),
                    Point(x = point[0, 0][0].toFloat(), y = point[0, 1][0].toFloat()),
                )
                val codeResult = CodeResult(CodeFormat.QR_CODE, string, corners)
                codeResults.add(codeResult)
            }
            onSuccess.invoke(codeResults)
        } catch (e: Throwable) {
            onFailure.invoke(e)
        }
    }
}
package per.goweii.codex.processor.wechat

import android.content.Context
import androidx.camera.core.ImageProxy
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.Point
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.wechat.internal.WeChatQRCodeDetector
import per.goweii.codex.scanner.ImageConverter
import java.util.concurrent.atomic.AtomicReference

class WeChatQRCodeScanProcessor(context: Context) : DecodeProcessor<ImageProxy> {
    private val detector = WeChatQRCodeDetector(context)
    private val reuseBuffer = AtomicReference<ByteArray?>(null)

    override fun process(
        input: ImageProxy,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val nv21 = ImageConverter.yuv420888toNv21(input, reuseBuffer.get())

        try {
            val width = input.width
            val height = input.height

            val mat = Mat(height + height / 2, width, CvType.CV_8UC1)
            mat.put(0, 0, nv21)
            val bgrMat = Mat()
            Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_YUV2BGR_NV21)
            mat.release()
            val points = arrayListOf<Mat>()
            val result = detector.detectAndDecode(bgrMat, points)
            bgrMat.release()

            if (result.isEmpty()) {
                throw CodeNotFoundException
            }

            val size = result.size
            val codeResults = ArrayList<CodeResult>(size)
            for (i in 0 until size) {
                val string = result[i]
                val point = points[i]
                val corners = arrayOf(
                    Point(x = point[0, 0][0].toFloat(), y = point[0, 1][0].toFloat()),
                    Point(x = point[1, 0][0].toFloat(), y = point[1, 1][0].toFloat()),
                    Point(x = point[2, 0][0].toFloat(), y = point[2, 1][0].toFloat()),
                    Point(x = point[3, 0][0].toFloat(), y = point[3, 1][0].toFloat()),
                )
                val codeResult = CodeResult(CodeFormat.QR_CODE, string, corners)
                codeResults.add(codeResult)
            }

            onSuccess.invoke(codeResults)
        } catch (e: Throwable) {
            reuseBuffer.set(nv21)
            onFailure.invoke(e)
        }
    }
}
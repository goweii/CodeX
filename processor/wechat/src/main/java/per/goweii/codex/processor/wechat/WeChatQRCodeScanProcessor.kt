package per.goweii.codex.processor.wechat

import android.content.Context
import androidx.camera.core.ImageProxy
import org.opencv.core.Core
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
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class WeChatQRCodeScanProcessor(context: Context) : DecodeProcessor<ImageProxy> {
    private val queue: Queue<ByteArray> = ConcurrentLinkedQueue()
    private val joined = AtomicBoolean(false)

    private val detector by lazy { WeChatQRCodeDetector(context) }

    override fun process(
        input: ImageProxy,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!joined.get()) {
            val imageSize = input.width * input.height
            val bytes = ByteArray(imageSize + 2 * (imageSize / 4))
            queue.add(bytes)
            joined.set(true)
        }

        val nv21Data = queue.poll()
        if (nv21Data == null) {
            onFailure.invoke(Exception("nv21Data == null"))
            return
        }

        try {
            ImageConverter.yuv420888toNv21(input, nv21Data)

            val width = input.width
            val height = input.height
            val rotation = input.imageInfo.rotationDegrees

            val mat = Mat(height + height / 2, width, CvType.CV_8UC1)
            mat.put(0, 0, nv21Data)
            val bgrMat = Mat()
            Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_YUV2BGR_NV21)
            mat.release()
            rotation(bgrMat, rotation)
            val points = arrayListOf<Mat>()
            val result = detector.detectAndDecode(bgrMat, points)
            bgrMat.release()

            if (result.isEmpty()) {
                throw CodeNotFoundException
            }

            joined.set(false)

            val size = result.size
            val codeResults = ArrayList<CodeResult>(size)
            for (i in 0 until size) {
                val string = result[i]
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
        } catch (e: Exception) {
            queue.add(nv21Data)
            onFailure.invoke(e)
        }
    }

    private fun rotation(mat: Mat, rotation: Int) {
        //  旋转90°
        when (rotation) {
            90 -> {
                // 将图像逆时针旋转90°，然后再关于x轴对称
                Core.transpose(mat, mat)
                // 然后再绕Y轴旋转180° （顺时针）
                Core.flip(mat, mat, 1)
            }

            180 -> {
                //将图片绕X轴旋转180°（顺时针）
                Core.flip(mat, mat, 0)
                //将图片绕Y轴旋转180°（顺时针）
                Core.flip(mat, mat, 1)
            }

            270 -> {
                // 将图像逆时针旋转90°，然后再关于x轴对称
                Core.transpose(mat, mat)
                // 将图片绕X轴旋转180°（顺时针）
                Core.flip(mat, mat, 0)
            }
        }
    }
}
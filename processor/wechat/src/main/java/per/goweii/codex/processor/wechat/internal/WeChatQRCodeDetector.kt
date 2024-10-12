package per.goweii.codex.processor.wechat.internal

import android.content.Context
import android.graphics.Bitmap
import com.king.wechat.qrcode.WeChatQRCodeDetector
import org.opencv.OpenCV
import org.opencv.core.Mat

internal class WeChatQRCodeDetector(context: Context) {
    init {
        ensureInited(context)
    }

    fun detectAndDecode(img: Mat, points: MutableList<Mat>): List<String> {
        return WeChatQRCodeDetector.detectAndDecode(img, points) ?: emptyList()
    }

    fun detectAndDecode(img: Bitmap, points: MutableList<Mat>): List<String> {
        return WeChatQRCodeDetector.detectAndDecode(img, points) ?: emptyList()
    }

    companion object {
        @JvmStatic
        @Volatile
        private var sInited: Boolean = false

        @JvmStatic
        private fun ensureInited(context: Context) {
            if (!sInited) {
                synchronized(WeChatQRCodeDetector::class.java) {
                    if (!sInited) {
                        OpenCV.initOpenCV()
                        WeChatQRCodeDetector.init(context)
                        sInited = true
                    }
                }
            }
        }
    }
}
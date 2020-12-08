package per.goweii.codex.decorator.autozoom

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.camera.core.ZoomState
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decoration.ScanDecoration

class AutoZoomDecoration(
    private val zoomDelay: Long = 3000L
) : ScanDecoration {
    private var zoomState: LiveData<ZoomState>? = null
    private var zoomObserver: Observer<ZoomState> = Observer {
        postAutoZoom()
    }
    private var handler: Handler? = null
    private var cameraProxy: CameraProxy? = null

    override fun onCreate(scanner: CodeScanner) {
        handler = Handler(Looper.getMainLooper())
    }

    override fun onBind(camera: CameraProxy) {
        cameraProxy = camera
        zoomState = camera.zoomState
        zoomState?.observeForever(zoomObserver)
        postAutoZoom()
    }

    private fun postAutoZoom() {
        handler?.removeCallbacksAndMessages(null)
        handler?.postDelayed({
            cameraProxy?.autoZoom()
            postAutoZoom()
        }, zoomDelay)
    }

    override fun onFound(results: List<CodeResult>, bitmap: Bitmap?) {
        handler?.removeCallbacksAndMessages(null)
    }

    override fun onUnbind() {
        cameraProxy = null
        zoomState?.removeObserver(zoomObserver)
        zoomState = null
    }

    override fun onDestroy() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
    }
}
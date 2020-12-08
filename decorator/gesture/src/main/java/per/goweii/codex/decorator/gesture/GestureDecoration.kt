package per.goweii.codex.decorator.gesture

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decoration.ScanDecoration

@SuppressLint("ClickableViewAccessibility")
class GestureDecoration : ScanDecoration {
    private var scanner: CodeScanner? = null

    override fun onCreate(scanner: CodeScanner) {
        this.scanner = scanner
        val gestureDetector = GestureDetector(scanner.previewView.context, GestureListener())
        val scaleGestureDetector =
            ScaleGestureDetector(scanner.previewView.context, ScaleGestureListener())
        scanner.previewView.setOnTouchListener { _, e ->
            gestureDetector.onTouchEvent(e)
            scaleGestureDetector.onTouchEvent(e)
            return@setOnTouchListener true
        }
    }

    override fun onBind(camera: CameraProxy) {
    }

    override fun onFound(results: List<CodeResult>, bitmap: Bitmap?) {
    }

    override fun onUnbind() {
    }

    override fun onDestroy() {
        scanner?.previewView?.setOnTouchListener(null)
        scanner = null
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            scanner?.cameraProxy?.startFocus()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            scanner?.cameraProxy?.toggleZoom()
            return true
        }
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scanner?.cameraProxy?.apply {
                startZoom(detector.scaleFactor * zoomRatio)
            }
            return true
        }
    }
}
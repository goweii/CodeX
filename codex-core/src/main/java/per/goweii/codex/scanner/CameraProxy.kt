package per.goweii.codex.scanner

import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.lifecycle.LiveData
import com.google.common.util.concurrent.ListenableFuture

class CameraProxy(
    val camera: Camera,
    val previewView: PreviewView
) {
    companion object {
        const val TORCH_OFF = TorchState.OFF
        const val TORCH_ON = TorchState.ON
    }

    private var focusFuture: ListenableFuture<FocusMeteringResult>? = null
    private var zoomFuture: ListenableFuture<Void>? = null

    val torchState: LiveData<Int>
        get() = camera.cameraInfo.torchState

    val zoomState: LiveData<ZoomState>
        get() = camera.cameraInfo.zoomState

    val zoomRatio: Float
        get() = camera.cameraInfo.zoomState.value?.zoomRatio ?: 0F

    val minZoomRatio: Float
        get() = camera.cameraInfo.zoomState.value?.minZoomRatio ?: 0F

    val maxZoomRatio: Float
        get() = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 0F

    val midZoomRatio: Float
        get() = (minZoomRatio + maxZoomRatio) * 0.382F

    fun startFocus() {
        camera.cameraControl.cancelFocusAndMetering()
        val w = previewView.width
        val h = previewView.height
        if (w <= 0 && h <= 0) return
        val cx = w * 0.5F
        val cy = h * 0.5F
        val mpf = previewView.meteringPointFactory
        val action = FocusMeteringAction.Builder(mpf.createPoint(cx, cy))
            .addPoint(mpf.createPoint(cx, cy * 0.5F))
            .addPoint(mpf.createPoint(cx, cy * 1.5F))
            .addPoint(mpf.createPoint(cx * 0.5F, cy))
            .addPoint(mpf.createPoint(cx * 1.5F, cy))
            .addPoint(mpf.createPoint(cx * 0.5F, cy * 0.5F))
            .addPoint(mpf.createPoint(cx * 1.5F, cy * 0.5F))
            .addPoint(mpf.createPoint(cx * 1.5F, cy * 1.5F))
            .addPoint(mpf.createPoint(cx * 0.5F, cy * 1.5F))
            .build()
        focusFuture?.cancel(true)
        focusFuture = camera.cameraControl.startFocusAndMetering(action)
    }

    fun startFocus(x: Float, y: Float) {
        camera.cameraControl.cancelFocusAndMetering()
        val point = previewView.meteringPointFactory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point)
            .addPoint(point)
            .build()
        focusFuture?.cancel(true)
        focusFuture = camera.cameraControl.startFocusAndMetering(action)
    }

    fun startZoom(zoomRatio: Float) {
        val zoomState = camera.cameraInfo.zoomState.value ?: return
        val curZoomRatio = zoomState.zoomRatio
        if (curZoomRatio == zoomRatio) return
        val maxZoomRatio = zoomState.maxZoomRatio
        val minZoomRatio = zoomState.minZoomRatio
        val ratio = when {
            zoomRatio < minZoomRatio -> minZoomRatio
            zoomRatio > maxZoomRatio -> maxZoomRatio
            else -> zoomRatio
        }
        zoomFuture?.cancel(true)
        zoomFuture = camera.cameraControl.setZoomRatio(ratio)
    }

    fun toggleZoom() {
        val ratio = when {
            zoomRatio < midZoomRatio -> midZoomRatio
            zoomRatio < maxZoomRatio -> maxZoomRatio
            else -> minZoomRatio
        }
        startZoom(ratio)
    }

    fun autoZoom() {
        when {
            zoomRatio < midZoomRatio -> startZoom(midZoomRatio)
            zoomRatio < maxZoomRatio -> startZoom(maxZoomRatio)
        }
    }

    fun enableTorch(enable: Boolean) {
        camera.cameraControl.enableTorch(enable)
    }
}
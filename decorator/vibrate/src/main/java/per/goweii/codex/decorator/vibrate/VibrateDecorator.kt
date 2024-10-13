package per.goweii.codex.decorator.vibrate

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import per.goweii.codex.CodeResult
import per.goweii.codex.MultiCodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decorator.ScanDecorator

class VibrateDecorator(
    private val duration: Long = 20L,
    private val amplitude: Int = 10
) : ScanDecorator {
    private var vibrator: Vibrator? = null

    private var lastResult = MultiCodeResult.empty

    override fun onCreate(scanner: CodeScanner) {
        vibrator = scanner.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onBind(camera: CameraProxy) {
        lastResult = MultiCodeResult.empty
    }

    override fun onFindSuccess(results: List<CodeResult>, bitmap: Bitmap?) {
        val oldResult = lastResult
        val newResult = MultiCodeResult(results)
        lastResult = newResult

        if (oldResult.sameTo(newResult)) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            vibrator?.vibrate(duration)
        }
    }

    override fun onFindFailure(e: Throwable) {
    }

    override fun onUnbind() {
        lastResult = MultiCodeResult.empty
    }

    override fun onDestroy() {
        vibrator = null
    }
}
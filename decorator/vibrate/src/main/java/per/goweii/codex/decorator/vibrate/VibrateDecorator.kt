package per.goweii.codex.decorator.vibrate

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decorator.ScanDecorator

class VibrateDecorator(
    private val duration: Long = 20L,
    private val amplitude: Int = 10
) : ScanDecorator {
    private var vibrator: Vibrator? = null

    override fun onCreate(scanner: CodeScanner) {
        vibrator = scanner.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onBind(camera: CameraProxy) {
    }

    override fun onFound(results: List<CodeResult>, bitmap: Bitmap?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            vibrator?.vibrate(duration)
        }
    }

    override fun onUnbind() {
    }

    override fun onDestroy() {
        vibrator = null
    }
}
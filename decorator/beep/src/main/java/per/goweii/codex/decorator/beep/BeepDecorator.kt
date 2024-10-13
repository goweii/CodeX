package per.goweii.codex.decorator.beep

import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.SoundPool
import per.goweii.codex.CodeResult
import per.goweii.codex.MultiCodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decorator.ScanDecorator

class BeepDecorator(
    private val volume: Float = 1.0F
) : ScanDecorator {
    private var soundPool: SoundPool? = null
    private var soundId: Int? = null

    private var lastResult = MultiCodeResult.empty

    override fun onCreate(scanner: CodeScanner) {
        soundPool?.release()
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()
        soundId = soundPool!!.load(scanner.context, R.raw.codex_decorator_beep, 1)
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

        val soundPool = soundPool ?: return
        val soundId = soundId ?: return
        soundPool.play(soundId, volume, volume, 0, 0, 1.0F)
    }

    override fun onFindFailure(e: Throwable) {
    }

    override fun onUnbind() {
        lastResult = MultiCodeResult.empty
    }

    override fun onDestroy() {
        soundPool?.release()
        soundPool = null
        soundId = null
    }
}
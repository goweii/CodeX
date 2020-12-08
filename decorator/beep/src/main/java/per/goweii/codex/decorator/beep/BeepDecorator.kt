package per.goweii.codex.decorator.beep

import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.SoundPool
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decorator.ScanDecorator

class BeepDecorator(
    private val volume: Float = 1.0F
) : ScanDecorator {
    private var soundPool: SoundPool? = null
    private var soundId: Int? = null

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
        soundId = soundPool!!.load(scanner.context, R.raw.codex_decoration_beep, 1)
    }

    override fun onBind(camera: CameraProxy) {
    }

    override fun onFound(results: List<CodeResult>, bitmap: Bitmap?) {
        val soundPool = soundPool ?: return
        val soundId = soundId ?: return
        soundPool.play(soundId, volume, volume, 0, 0, 1.0F)
    }

    override fun onUnbind() {
    }

    override fun onDestroy() {
        soundPool?.release()
        soundPool = null
        soundId = null
    }
}
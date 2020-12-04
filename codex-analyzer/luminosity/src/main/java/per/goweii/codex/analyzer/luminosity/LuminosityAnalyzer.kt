package per.goweii.codex.analyzer.luminosity

import android.os.Handler
import android.os.Looper
import androidx.camera.core.ImageProxy
import per.goweii.codex.scanner.analyzer.ScanAnalyzer

class LuminosityAnalyzer(
    private val minAnalyzeDuration: Long = 1000L,
    private val onLuminosity: (Double) -> Unit
) : ScanAnalyzer {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastAnalyzeTime = 0L

    override fun analyze(image: ImageProxy, chain: ScanAnalyzer.Chain) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalyzeTime > minAnalyzeDuration) {
            val yBuffer = image.planes[0].buffer
            yBuffer.rewind()
            val ySize = yBuffer.remaining()
            var sum = 0.0
            for (i in 0 until ySize) {
                sum += (yBuffer.get(i).toInt() and 0xFF)
            }
            val luminosity = if (ySize == 0) Double.NaN else sum / ySize
            lastAnalyzeTime = currentTime
            mainHandler.post {
                onLuminosity(luminosity)
            }
        }
        chain.next(image)
    }
}
package per.goweii.codex.scanner.analyzer

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.scanner.ImageConverter

class DecodeAnalyzer(
    val processor: DecodeProcessor<ImageProxy>,
    private val callback: Callback
) : ScanAnalyzer {
    override fun analyze(image: ImageProxy, chain: ScanAnalyzer.Chain) {
        processor.process(
            image,
            onSuccess = { results ->
                if (!chain.isShutdown()) {
                    chain.shutdown()
                    results.forEach {
                        it.mapToPercent(
                            image.width.toFloat(),
                            image.height.toFloat()
                        )
                    }
                    val bitmap = ImageConverter.imageToBitmap(image)
                    callback.onSuccess(results, bitmap)
                }
                chain.next(image)
            },
            onFailure = { e ->
                callback.onFailure(e)
                chain.next(image)
            }
        )
    }

    interface Callback {
        fun onSuccess(results: List<CodeResult>, bitmap: Bitmap?)
        fun onFailure(e: Exception)
    }
}
package per.goweii.codex.scanner.analyzer

import androidx.camera.core.ImageProxy
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor

class DecodeAnalyzer(
    val processor: DecodeProcessor<ImageProxy>,
    private val callback: Callback
) : ScanAnalyzer {
    override fun analyze(image: ImageProxy, chain: ScanAnalyzer.Chain) {
        processor.process(
            image,
            onSuccess = { results ->
                results.forEach {
                    it.mapToPercent(
                        image.width.toFloat(),
                        image.height.toFloat()
                    )
                }
                callback.onSuccess(results, image)
                chain.next(image)
            },
            onFailure = { e ->
                callback.onFailure(e)
                chain.next(image)
            }
        )
    }

    interface Callback {
        fun onSuccess(results: List<CodeResult>, image: ImageProxy)
        fun onFailure(e: Throwable)
    }
}
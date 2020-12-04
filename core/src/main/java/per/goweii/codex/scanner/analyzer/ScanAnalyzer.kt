package per.goweii.codex.scanner.analyzer

import androidx.camera.core.ImageProxy

/**
 * @author CuiZhen
 * @date 2020/11/15
 */
interface ScanAnalyzer {
    fun analyze(image: ImageProxy, chain: Chain)

    interface Chain {
        fun isShutdown(): Boolean
        fun next(image: ImageProxy)
        fun restart()
        fun shutdown()
    }
}
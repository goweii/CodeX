package per.goweii.codex.scanner.decoration

import android.graphics.Bitmap
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner

/**
 * @author CuiZhen
 * @date 2020/11/15
 */
internal class DecorationSet {
    private val decorations = arrayListOf<ScanDecoration>()

    fun contains(clazz: Class<out ScanDecoration>): Boolean {
        decorations.forEach {
            if (it.javaClass.name == clazz.name) {
                return true
            }
        }
        return false
    }

    fun appendUnique(analyzer: ScanDecoration) {
        if (!contains(analyzer.javaClass)) {
            append(analyzer)
        }
    }

    fun replace(analyzer: ScanDecoration) {
        remove(analyzer.javaClass)
        append(analyzer)
    }

    fun append(vararg decoration: ScanDecoration) {
        this.decorations.addAll(decoration)
    }

    fun remove(clazz: Class<out ScanDecoration>) {
        val iterator = decorations.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().javaClass.name == clazz.name) {
                iterator.remove()
            }
        }
    }

    fun clear() {
        this.decorations.clear()
    }

    internal fun onCreate(scanner: CodeScanner) {
        decorations.forEach {
            it.onCreate(scanner)
        }
    }

    internal fun onBind(cameraProxy: CameraProxy) {
        decorations.forEach {
            it.onBind(cameraProxy)
        }
    }

    internal fun onFound(results: List<CodeResult>, bitmap: Bitmap?) {
        decorations.forEach {
            it.onFound(results, bitmap)
        }
    }

    internal fun onUnbind() {
        decorations.forEach {
            it.onUnbind()
        }
    }

    internal fun onDestroy() {
        decorations.forEach {
            it.onDestroy()
        }
    }
}
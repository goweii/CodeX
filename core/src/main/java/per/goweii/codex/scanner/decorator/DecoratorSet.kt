package per.goweii.codex.scanner.decorator

import android.graphics.Bitmap
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner

/**
 * @author CuiZhen
 * @date 2020/11/15
 */
internal class DecoratorSet {
    private val decorators = arrayListOf<ScanDecorator>()

    fun contains(clazz: Class<out ScanDecorator>): Boolean {
        decorators.forEach {
            if (it.javaClass.name == clazz.name) {
                return true
            }
        }
        return false
    }

    fun appendUnique(analyzer: ScanDecorator) {
        if (!contains(analyzer.javaClass)) {
            append(analyzer)
        }
    }

    fun replace(analyzer: ScanDecorator) {
        remove(analyzer.javaClass)
        append(analyzer)
    }

    fun append(vararg decorator: ScanDecorator) {
        this.decorators.addAll(decorator)
    }

    fun remove(clazz: Class<out ScanDecorator>) {
        val iterator = decorators.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().javaClass.name == clazz.name) {
                iterator.remove()
            }
        }
    }

    fun clear() {
        this.decorators.clear()
    }

    internal fun onCreate(scanner: CodeScanner) {
        decorators.forEach {
            it.onCreate(scanner)
        }
    }

    internal fun onBind(cameraProxy: CameraProxy) {
        decorators.forEach {
            it.onBind(cameraProxy)
        }
    }

    internal fun onFindSuccess(results: List<CodeResult>, bitmap: Bitmap?) {
        decorators.forEach {
            it.onFindSuccess(results, bitmap)
        }
    }

    internal fun onFindFailure(e: Throwable) {
        decorators.forEach {
            it.onFindFailure(e)
        }
    }

    internal fun onUnbind() {
        decorators.forEach {
            it.onUnbind()
        }
    }

    internal fun onDestroy() {
        decorators.forEach {
            it.onDestroy()
        }
    }
}
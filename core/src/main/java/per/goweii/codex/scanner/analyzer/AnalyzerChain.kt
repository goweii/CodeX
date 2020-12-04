package per.goweii.codex.scanner.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal class AnalyzerChain : ImageAnalysis.Analyzer, ScanAnalyzer.Chain {
    private val analyzers = CopyOnWriteArrayList<ScanAnalyzer>()
    private val localIndex = AtomicInteger(0)
    private val shutdown = AtomicBoolean(false)

    val size: Int get() = analyzers.size
    val iterator: MutableIterator<ScanAnalyzer> get() = analyzers.iterator()

    fun append(analyzer: ScanAnalyzer) {
        analyzers.add(analyzer)
    }

    fun appendUnique(analyzer: ScanAnalyzer) {
        if (!contains(analyzer.javaClass)) {
            append(analyzer)
        }
    }

    fun replace(analyzer: ScanAnalyzer) {
        remove(analyzer.javaClass)
        append(analyzer)
    }

    fun remove(clazz: Class<out ScanAnalyzer>) {
        val iterator = analyzers.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().javaClass.name == clazz.name) {
                iterator.remove()
            }
        }
    }

    fun contains(clazz: Class<out ScanAnalyzer>): Boolean {
        analyzers.forEach {
            if (it.javaClass.name == clazz.name) {
                return true
            }
        }
        return false
    }

    fun clear() {
        analyzers.clear()
    }

    override fun analyze(image: ImageProxy) {
        localIndex.set(0)
        doAnalyze(image)
    }

    override fun isShutdown(): Boolean {
        return shutdown.get()
    }

    override fun next(image: ImageProxy) {
        localIndex.incrementAndGet()
        doAnalyze(image)
    }

    override fun restart() {
        localIndex.set(0)
        shutdown.set(false)
    }

    override fun shutdown() {
        shutdown.set(true)
    }

    private fun doAnalyze(image: ImageProxy) {
        if (shutdown.get()) {
            image.close()
            return
        }
        val index = localIndex.get()
        if (index < 0 || index >= size) {
            image.close()
            return
        }
        analyzers[index].analyze(image, this)
    }
}
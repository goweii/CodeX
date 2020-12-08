package per.goweii.codex.scanner

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.scanner.analyzer.AnalyzerChain
import per.goweii.codex.scanner.analyzer.DecodeAnalyzer
import per.goweii.codex.scanner.analyzer.ScanAnalyzer
import per.goweii.codex.scanner.decorator.DecoratorSet
import per.goweii.codex.scanner.decorator.ScanDecorator
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CodeScanner : FrameLayout, DecodeAnalyzer.Callback {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val analyzerChain = AnalyzerChain()
    private val decorationSet = DecoratorSet()
    private val results = mutableListOf<CodeResult>()

    private val ratioMutableLiveData: MutableLiveData<Ratio> = MutableLiveData(Ratio.RATIO_16_9)
    private val cameraProxyMutableLiveData: MutableLiveData<CameraProxy?> = MutableLiveData(null)

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analyzerUseCase: ImageAnalysis? = null
    private var analyzerExecutor: ExecutorService? = null

    private var lifecycleOwner: LifecycleOwner? = null
    private var lifecycleObserver: LifecycleObserver? = null

    private var isFirstAttach = true
    private var isPermissionGranted = false

    private var onFound: ((List<CodeResult>) -> Unit)? = null

    val previewView: PreviewView

    var cameraProxy: CameraProxy?
        get() = cameraProxyLiveData.value
        private set(value) {
            cameraProxyMutableLiveData.value = value
        }
    val cameraProxyLiveData: LiveData<CameraProxy?> = cameraProxyMutableLiveData

    var ratio: Ratio
        get() = ratioLiveData.value!!
        private set(value) {
            ratioMutableLiveData.value = value
        }
    val ratioLiveData: LiveData<Ratio> = ratioMutableLiveData

    val resultsCopy: List<CodeResult>
        get() = arrayListOf<CodeResult>().apply {
            results.forEach { add(it.copy()) }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        previewView = PreviewView(context)
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        addViewInLayout(previewView, 0, generateDefaultLayoutParams())
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            startScanIfReady()
        }, ContextCompat.getMainExecutor(context))
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isFirstAttach) {
            isFirstAttach = false
            decorationSet.onCreate(this)
        }
        startScanIfReady()
    }

    private fun onDestroy() {
        unbindLifecycleObserver()
        this.lifecycleOwner = null
        this.lifecycleObserver = null
        stopScanIfNeed()
        decorationSet.onDestroy()
        analyzerExecutor?.shutdownNow()
        analyzerExecutor = null
    }

    fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        if (this.lifecycleOwner != lifecycleOwner) {
            unbindLifecycleObserver()
        }
        this.lifecycleOwner = lifecycleOwner
        bindLifecycleObserver()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun startScan() {
        isPermissionGranted = true
        results.clear()
        startScanIfReady()
    }

    fun stopScan() {
        stopScanIfNeed()
    }

    fun enableTorch(enable: Boolean) {
        cameraProxy?.enableTorch(enable)
    }

    fun onFound(callback: (List<CodeResult>) -> Unit) {
        this.onFound = callback
    }

    fun addDecoration(vararg decorator: ScanDecorator) {
        decorationSet.append(*decorator)
    }

    fun removeDecoration(clazz: Class<out ScanDecorator>) {
        decorationSet.remove(clazz)
    }

    fun clearDecoration() {
        decorationSet.clear()
    }

    fun addAnalyzer(vararg analyzer: ScanAnalyzer) {
        analyzer.forEach {
            this.analyzerChain.append(it)
        }
    }

    fun removeAnalyzer(clazz: Class<out ScanAnalyzer>) {
        analyzerChain.remove(clazz)
    }

    fun clearAnalyzer() {
        analyzerChain.clear()
    }

    fun addProcessor(vararg processor: DecodeProcessor<ImageProxy>) {
        processor.forEach {
            val analyzer = DecodeAnalyzer(it, this)
            this.analyzerChain.append(analyzer)
        }
    }

    fun removeProcessor(clazz: Class<out DecodeProcessor<ImageProxy>>) {
        val iterator = analyzerChain.iterator
        while (iterator.hasNext()) {
            val analyzer = iterator.next()
            if (analyzer is DecodeAnalyzer) {
                if (analyzer.processor.javaClass.name == clazz.name) {
                    iterator.remove()
                }
            }
        }
    }

    fun clearProcessor() {
        analyzerChain.remove(DecodeAnalyzer::class.java)
    }

    fun doOnStreaming(block: () -> Unit) {
        val lifecycleOwner = lifecycleOwner ?: return
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return
        if (previewView.previewStreamState.value == PreviewView.StreamState.STREAMING) {
            block.invoke()
            return
        }
        previewView.previewStreamState.observe(
            lifecycleOwner,
            object : Observer<PreviewView.StreamState> {
                override fun onChanged(state: PreviewView.StreamState) {
                    if (state == PreviewView.StreamState.STREAMING) {
                        previewView.previewStreamState.removeObserver(this)
                        block.invoke()
                    }
                }
            })
    }

    private fun bindLifecycleObserver() {
        val lifecycleOwner = lifecycleOwner ?: return
        lifecycleObserver = ScannerLifecycleObserver()
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver!!)
    }

    private fun unbindLifecycleObserver() {
        val lifecycleOwner = lifecycleOwner ?: return
        val lifecycleObserver = lifecycleObserver ?: return
        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
    }

    private fun startScanIfReady() {
        if (!isPermissionGranted) return
        val lifecycleOwner = lifecycleOwner ?: return
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return
        if (cameraProxy != null) return
        if (cameraProvider == null) return
        doAfterPreviewViewAttached {
            bindCameraUseCases()?.let { camera ->
                cameraProxy = CameraProxy(camera, previewView)
                analyzerChain.restart()
                decorationSet.onBind(cameraProxy!!)
            }
        }
    }

    private fun stopScanIfNeed() {
        if (cameraProxy == null) return
        unbindCameraUseCases()
        cameraProxy = null
    }

    private fun bindCameraUseCases(): Camera? {
        if (!isPermissionGranted) return null
        val lifecycleOwner = lifecycleOwner ?: return null
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return null
        val cameraProvider = cameraProvider ?: return null
        cameraProvider.unbindAll()
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase)
            previewUseCase = null
        }
        if (analyzerUseCase != null) {
            cameraProvider.unbind(analyzerUseCase)
            analyzerUseCase = null
        }
        updateRatioByDisplayMetrics()
        val rotation = previewView.display.rotation
        previewUseCase = Preview.Builder()
            .setTargetAspectRatio(ratio.aspectRatio)
            .setTargetRotation(rotation)
            .build()
            .apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
        analyzerUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(ratio.aspectRatio)
            .setTargetRotation(rotation)
            .build()
            .apply {
                if (analyzerExecutor == null || analyzerExecutor!!.isShutdown) {
                    analyzerExecutor = Executors.newSingleThreadExecutor()
                }
                setAnalyzer(analyzerExecutor!!, analyzerChain)
            }
        return try {
            val lensFacing = when {
                cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.LENS_FACING_BACK
                cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                analyzerUseCase
            )
        } catch (e: Exception) {
            analyzerChain.shutdown()
            previewUseCase?.setSurfaceProvider(null)
            analyzerUseCase?.clearAnalyzer()
            previewUseCase = null
            analyzerUseCase = null
            null
        }
    }

    private fun unbindCameraUseCases() {
        analyzerUseCase?.clearAnalyzer()
        previewUseCase?.setSurfaceProvider(null)
        decorationSet.onUnbind()
        analyzerChain.shutdown()
        cameraProvider?.unbindAll()
        previewUseCase = null
        analyzerUseCase = null
    }

    private fun updateRatioByDisplayMetrics() {
        ratio = if (previewView.display != null) {
            val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val previewRatio = max(width, height).toDouble() / min(width, height)
            if (abs(previewRatio - Ratio.RATIO_4_3.floatValue) <= abs(previewRatio - Ratio.RATIO_16_9.floatValue)) {
                Ratio.RATIO_4_3
            } else {
                Ratio.RATIO_16_9
            }
        } else {
            Ratio.RATIO_16_9
        }
    }

    private fun doAfterPreviewViewAttached(block: () -> Unit) {
        if (previewView.display != null) {
            block.invoke()
            return
        }
        previewView.viewTreeObserver.addOnWindowAttachListener(object :
            ViewTreeObserver.OnWindowAttachListener {
            override fun onWindowAttached() {
                previewView.viewTreeObserver.removeOnWindowAttachListener(this)
                block.invoke()
            }

            override fun onWindowDetached() {
            }
        })
    }

    override fun onSuccess(results: List<CodeResult>, bitmap: Bitmap?) {
        results.forEach { it.rotate90(1F, 1F) }
        this.results.clear()
        this.results.addAll(results)
        var frozenBitmap: Bitmap? = null
        if (bitmap != null) {
            frozenBitmap = ImageConverter.bitmapRotation90(bitmap)
        }
        if (frozenBitmap == null) {
            frozenBitmap = previewView.bitmap
        }
        mainHandler.post {
            onFound?.invoke(resultsCopy)
            decorationSet.onFound(resultsCopy, frozenBitmap)
            stopScan()
        }
    }

    override fun onFailure(e: Exception) {
    }

    inner class ScannerLifecycleObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(owner: LifecycleOwner) {
            onDestroy()
        }
    }

    enum class Ratio(
        val floatValue: Float,
        val aspectRatio: Int
    ) {
        RATIO_16_9(16F / 9F, AspectRatio.RATIO_16_9),
        RATIO_4_3(4F / 3F, AspectRatio.RATIO_4_3),
    }
}
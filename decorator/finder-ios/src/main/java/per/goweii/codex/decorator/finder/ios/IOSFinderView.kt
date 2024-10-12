package per.goweii.codex.decorator.finder.ios

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.animation.doOnEnd
import androidx.core.content.res.use
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import per.goweii.codex.CodeResult
import per.goweii.codex.PerspectiveTransform
import per.goweii.codex.Point
import per.goweii.codex.Quad
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decorator.ScanDecorator
import kotlin.math.min

class IOSFinderView : View, ScanDecorator {
    @ColorInt
    private var finderNormalColor = Color.WHITE

    @ColorInt
    private var finderFoundColor = Color.GREEN

    @ColorInt
    private var finderCircleOuterColor = Color.WHITE

    @ColorInt
    private var finderCircleInnerColor = Color.GREEN

    @FloatRange(from = 0.0, to = 1.0)
    private var finderInitSize = 0.8F

    @FloatRange(from = 0.0, to = 1.0)
    private var finderNormalSize = 0.6F

    @FloatRange(from = 0.0, to = 1.0)
    private var finderZoomSize = 0.7F

    @FloatRange(from = 0.0, to = 0.5)
    private var finderStrokeWidth = 0.03F

    @FloatRange(from = 0.0, to = 0.5)
    private var finderCornerRadius = 0.14F

    @FloatRange(from = 0.0, to = 0.5)
    private var finderSideLength = 0.02F

    @FloatRange(from = 0.0, to = 0.5)
    private var finderIsCircleRadius = 0.0618F

    @IntRange(from = 0)
    private var finderZoomDuration = 1000

    @IntRange(from = 0)
    private var finderSingleTransformDuration = 500

    @IntRange(from = 0)
    private var finderMultiCenterDuration = 500

    private var previewWidth: Int = 0
    private var previewHeight: Int = 0

    private var normalQuad: Quad = Quad.zero()
    private var finderQuad: Quad = Quad.zero()
    private val finderPath = Path()

    private val results = mutableListOf<CodeResult>()

    private val isFound: Boolean
        get() = results.isNotEmpty()
    private val isFoundSingle: Boolean
        get() = results.size == 1
    private val isFoundMulti: Boolean
        get() = results.size > 1
    private val isFinderLikeCircle: Boolean
        get() = finderQuad.minSideLength <= normalQuad.minSideLength * finderIsCircleRadius * 2F

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
    }

    private var singleTransformAnim: ValueAnimator? = null
    private var multiCenterAnim: ValueAnimator? = null
    private var zoomAnim: ValueAnimator? = null

    private var scanner: CodeScanner? = null

    private var ratioLiveData: LiveData<CodeScanner.Ratio>? = null
    private val ratioObserver: Observer<CodeScanner.Ratio> = Observer {
        requestLayout()
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.IOSFinderView, defStyleAttr, 0).use {
            finderNormalColor = it.getColor(
                R.styleable.IOSFinderView_finderNormalColor,
                finderNormalColor
            )
            finderFoundColor = it.getColor(
                R.styleable.IOSFinderView_finderFoundColor,
                finderFoundColor
            )
            finderCircleOuterColor = it.getColor(
                R.styleable.IOSFinderView_finderCircleOuterColor,
                finderCircleOuterColor
            )
            finderCircleInnerColor = it.getColor(
                R.styleable.IOSFinderView_finderCircleInnerColor,
                finderCircleInnerColor
            )
            finderInitSize = it.getFraction(
                R.styleable.IOSFinderView_finderInitSize,
                1, 1,
                finderInitSize
            )
            finderNormalSize = it.getFraction(
                R.styleable.IOSFinderView_finderNormalSize,
                1, 1,
                finderNormalSize
            )
            finderZoomSize = it.getFraction(
                R.styleable.IOSFinderView_finderZoomSize,
                1, 1,
                finderZoomSize
            )
            finderStrokeWidth = it.getFraction(
                R.styleable.IOSFinderView_finderStrokeWidth,
                1, 1,
                finderStrokeWidth
            )
            finderCornerRadius = it.getFraction(
                R.styleable.IOSFinderView_finderCornerRadius,
                1, 1,
                finderCornerRadius
            )
            finderSideLength = it.getFraction(
                R.styleable.IOSFinderView_finderSideLength,
                1, 1,
                finderSideLength
            )
            finderIsCircleRadius = it.getFraction(
                R.styleable.IOSFinderView_finderIsCircleRadius,
                1, 1,
                finderIsCircleRadius
            )
            finderZoomDuration = it.getInteger(
                R.styleable.IOSFinderView_finderZoomDuration,
                finderZoomDuration
            )
            finderSingleTransformDuration = it.getInteger(
                R.styleable.IOSFinderView_finderSingleTransformDuration,
                finderSingleTransformDuration
            )
            finderMultiCenterDuration = it.getInteger(
                R.styleable.IOSFinderView_finderMultiCenterDuration,
                finderMultiCenterDuration
            )
        }
    }

    override fun onCreate(scanner: CodeScanner) {
        this.scanner = scanner
        ratioLiveData = scanner.ratioLiveData
        scanner.ratioLiveData.observeForever(ratioObserver)
    }

    override fun onBind(camera: CameraProxy) {
        results.clear()
        cancelAllAnim()

        val startAnimTask = {
            val from = if (finderQuad.isZero) {
                normalQuad.copy().apply {
                    scaleByCenter(
                        finderInitSize / finderNormalSize,
                        finderInitSize / finderNormalSize
                    )
                }
            } else {
                finderQuad.copy()
            }
            val to = normalQuad.copy()
            singleTransformAnim = createSingleTransformAnim(from, to)
            singleTransformAnim?.doOnEnd {
                if (!isFound) {
                    startZoom()
                }
            }
            singleTransformAnim?.start()
        }

        scanner?.doOnStreaming {
            doOnPreDraw { startAnimTask() }
        } ?: doOnPreDraw { startAnimTask() }
    }

    override fun onFound(results: List<CodeResult>, bitmap: Bitmap?) {
        this.results.clear()
        this.results.addAll(results)
        cancelAllAnim()
        doOnPreDraw {
            this.results.forEach {
                it.mapFromPercent(previewWidth.toFloat(), previewHeight.toFloat())
            }
            invalidate()
            if (isFoundSingle) {
                foundSingle()
            } else {
                foundMulti()
            }
        }
    }

    override fun onUnbind() {
    }

    override fun onDestroy() {
        scanner = null
        ratioLiveData?.removeObserver(ratioObserver)
        ratioLiveData = null
        cancelAllAnim()
    }

    override fun onDetachedFromWindow() {
        cancelAllAnim()
        super.onDetachedFromWindow()
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val aspectRatio = if (isInEditMode) {
            CodeScanner.Ratio.RATIO_16_9.floatValue
        } else {
            ratioLiveData?.value?.floatValue ?: 0F
        }
        if (aspectRatio <= 0F) {
            setMeasuredDimension(0, 0)
            return
        }
        val finderWidth = MeasureSpec.getSize(widthMeasureSpec)
        val finderHeight = MeasureSpec.getSize(heightMeasureSpec)
        val sizeRatio = finderHeight.toFloat() / finderWidth.toFloat()
        if (aspectRatio > sizeRatio) {
            previewWidth = finderWidth
            previewHeight = (finderWidth * aspectRatio).toInt()
        } else if (aspectRatio < sizeRatio) {
            previewWidth = (finderHeight / aspectRatio).toInt()
            previewHeight = finderHeight
        }
        val finderRadius = min(finderWidth, finderHeight) / 2F
        val quad = Quad(
            lb = Point(-finderRadius, -finderRadius),
            lt = Point(-finderRadius, finderRadius),
            rt = Point(finderRadius, finderRadius),
            rb = Point(finderRadius, -finderRadius)
        ).apply {
            offset(finderWidth / 2F, finderHeight / 2F)
            offset(
                (previewWidth - finderWidth) / 2F,
                (previewHeight - finderHeight) / 2F
            )
        }
        quad.copy().apply { scaleByCenter(finderNormalSize, finderNormalSize) }.also {
            if (normalQuad != it) {
                normalQuad = it
            }
        }
        quad.copy().apply { scaleByCenter(finderInitSize, finderInitSize) }.also {
            if (finderQuad.isZero) {
                finderQuad = it
                updateFinderPath()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 && height <= 0) {
            return
        }
        canvas.save()
        canvas.translate(
            (width - previewWidth).toFloat() / 2F,
            (height - previewHeight).toFloat() / 2F
        )
        drawOutline(canvas)
        drawResults(canvas)
        canvas.restore()
    }

    private fun startZoom() {
        cancelAllAnim()
        zoomAnim = createZoomAnim()
        zoomAnim?.start()
    }

    private fun foundSingle() {
        val from = finderQuad.copy()
        val to = results[0].quad.copy().apply {
            if (isPoint) {
                expand(0.5F)
            } else {
                val l = maxSideLength
                val rect = Quad.zero()
                rect.expand(l / 2F)
                val transform = PerspectiveTransform.quadrilateralToQuadrilateral(rect, this)
                val expandRect = rect.apply {
                    expand(finderStrokeWidth * l * 0.5F + finderCornerRadius * l * 0.35F)
                }
                transform.transformQuad(expandRect)
                set(expandRect)
            }
        }
        singleTransformAnim = createSingleTransformAnim(from, to)
        singleTransformAnim?.start()
    }

    private fun foundMulti() {
        finderQuad = Quad.zero()
        finderPath.reset()
        finderPath.rewind()
        multiCenterAnim = createMultiCenterAnim()
        multiCenterAnim?.start()
    }

    private fun drawOutline(canvas: Canvas) {
        if (!BuildConfig.DEBUG) return
        paint.apply {
            color = Color.RED
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = 3F
        }
        results.forEach { result ->
            val quad = result.quad
            canvas.drawLine(quad.lb.x, quad.lb.y, quad.lt.x, quad.lt.y, paint)
            canvas.drawLine(quad.lt.x, quad.lt.y, quad.rt.x, quad.rt.y, paint)
            canvas.drawLine(quad.rt.x, quad.rt.y, quad.rb.x, quad.rb.y, paint)
            canvas.drawLine(quad.rb.x, quad.rb.y, quad.lb.x, quad.lb.y, paint)
        }
    }

    private fun drawResults(canvas: Canvas) {
        canvas.save()
        if (isFound) {
            if (isFoundSingle) {
                if (isFinderLikeCircle) {
                    val center = finderQuad.center
                    drawPoint(canvas, center, finderIsCircleRadius * normalQuad.minSideLength)
                } else {
                    drawFinder(canvas)
                }
            } else {
                val f = multiCenterAnim?.animatedFraction ?: 1F
                results.forEach { result ->
                    drawPoint(
                        canvas,
                        result.center,
                        f * finderIsCircleRadius * normalQuad.minSideLength
                    )
                }
            }
        } else {
            drawFinder(canvas)
        }
        canvas.restore()
    }

    private fun drawFinder(canvas: Canvas) {
        canvas.drawPath(finderPath, paint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = if (isFound) finderFoundColor else finderNormalColor
            style = Paint.Style.STROKE
            strokeWidth = finderQuad.maxSideLength * kotlin.run {
                val min = finderStrokeWidth * 3F
                val max = finderStrokeWidth
                val f = (1F - finderQuad.maxSideLength / normalQuad.maxSideLength)
                    .coerceAtLeast(0F)
                (min - max) * f + max
            }
        })
    }

    private fun drawPoint(canvas: Canvas, point: Point, r: Float) {
        canvas.drawCircle(point.x, point.y, r, paint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = finderCircleOuterColor
            style = Paint.Style.FILL
            strokeWidth = 0F
        })
        canvas.drawCircle(point.x, point.y, r * 0.618F, paint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = finderCircleInnerColor
            style = Paint.Style.FILL
            strokeWidth = 0F
        })
    }

    private fun cancelAllAnim() {
        zoomAnim?.cancel()
        zoomAnim = null
        singleTransformAnim?.cancel()
        singleTransformAnim = null
        multiCenterAnim?.cancel()
        multiCenterAnim = null
    }

    private fun createZoomAnim(): ValueAnimator {
        val from = normalQuad.copy()
        val to = normalQuad.copy().apply {
            scaleByCenter(finderZoomSize / finderNormalSize, finderZoomSize / finderNormalSize)
        }
        return ValueAnimator.ofObject(Quad.QuadEvaluator(), from, to).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = finderZoomDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                finderQuad = animator.animatedValue as Quad
                updateFinder()
            }
            doOnEnd {
                zoomAnim = null
            }
        }
    }

    private fun createSingleTransformAnim(from: Quad, to: Quad): ValueAnimator {
        return ValueAnimator.ofObject(Quad.QuadEvaluator(), from, to).apply {
            duration = finderSingleTransformDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                finderQuad = animator.animatedValue as Quad
                updateFinder()
            }
            doOnEnd {
                singleTransformAnim = null
            }
        }
    }

    private fun createMultiCenterAnim(): ValueAnimator {
        return ValueAnimator.ofFloat(0F, 1F).apply {
            duration = finderMultiCenterDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                updateMultiCenter()
            }
            doOnEnd {
                multiCenterAnim = null
            }
        }
    }

    private fun updateMultiCenter() {
        invalidate()
    }

    private fun updateFinder() {
        updateFinderPath()
        invalidate()
    }

    private fun updateFinderPath() {
        finderPath.reset()
        finderPath.rewind()
        val from = Quad(
            lb = Point(-0.5F, -0.5F),
            lt = Point(-0.5F, 0.5F),
            rt = Point(0.5F, 0.5F),
            rb = Point(0.5F, -0.5F)
        )
        val transform = PerspectiveTransform.quadrilateralToQuadrilateral(from, finderQuad)
        fun Path.addCorner(ps: FloatArray) {
            transform.transformPoints(ps)
            moveTo(ps[0], ps[1])
            lineTo(ps[2], ps[3])
            quadTo(ps[4], ps[5], ps[6], ps[7])
            lineTo(ps[8], ps[9])
        }
        finderPath.addCorner(
            floatArrayOf(
                from.lb.x + finderCornerRadius + finderSideLength, from.lb.y,
                from.lb.x + finderCornerRadius, from.lb.y,
                from.lb.x, from.lb.y,
                from.lb.x, from.lb.y + finderCornerRadius,
                from.lb.x, from.lb.y + finderCornerRadius + finderSideLength
            )
        )
        finderPath.addCorner(
            floatArrayOf(
                from.lt.x + finderCornerRadius + finderSideLength, from.lt.y,
                from.lt.x + finderCornerRadius, from.lt.y,
                from.lt.x, from.lt.y,
                from.lt.x, from.lt.y - finderCornerRadius,
                from.lt.x, from.lt.y - finderCornerRadius - finderSideLength
            )
        )
        finderPath.addCorner(
            floatArrayOf(
                from.rt.x - finderCornerRadius - finderSideLength, from.rt.y,
                from.rt.x - finderCornerRadius, from.rt.y,
                from.rt.x, from.rt.y,
                from.rt.x, from.rt.y - finderCornerRadius,
                from.rt.x, from.rt.y - finderCornerRadius - finderSideLength
            )
        )
        finderPath.addCorner(
            floatArrayOf(
                from.rb.x - finderCornerRadius - finderSideLength, from.rb.y,
                from.rb.x - finderCornerRadius, from.rb.y,
                from.rb.x, from.rb.y,
                from.rb.x, from.rb.y + finderCornerRadius,
                from.rb.x, from.rb.y + finderCornerRadius + finderSideLength
            )
        )
    }
}
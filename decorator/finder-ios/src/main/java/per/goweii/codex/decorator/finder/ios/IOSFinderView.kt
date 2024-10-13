package per.goweii.codex.decorator.finder.ios

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import per.goweii.codex.CodeResult
import per.goweii.codex.MultiCodeResult
import per.goweii.codex.PerspectiveTransform
import per.goweii.codex.Point
import per.goweii.codex.Quad
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decorator.ScanDecorator
import kotlin.math.max
import kotlin.math.min

class IOSFinderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ScanDecorator {
    private val normalConfig = IOSFinderConfig.fromAttr(context, attrs, defStyleAttr)
    private var normalQuad = Quad.zero()

    private val finderConfig = IOSFinderConfig()
    private var finderQuad = Quad.zero()
    private val finderPath = Path()

    private var previewWidth: Int = 0
    private var previewHeight: Int = 0

    private var result = MultiCodeResult.empty

    private val isFinderLikeCircle: Boolean
        get() = finderQuad.minSideLength <= normalQuad.minSideLength * normalConfig.isCircleRadius * 2F

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var singleTransformAnim: ValueAnimator? = null
    private var multiCenterAnim: ValueAnimator? = null
    private var zoomAnim: ValueAnimator? = null

    private var scanner: CodeScanner? = null

    private var ratioLiveData: LiveData<CodeScanner.Ratio>? = null
    private val ratioObserver: Observer<CodeScanner.Ratio> = Observer {
        requestLayout()
    }

    override fun onCreate(scanner: CodeScanner) {
        this.scanner = scanner
        ratioLiveData = scanner.ratioLiveData
        scanner.ratioLiveData.observeForever(ratioObserver)
    }

    override fun onBind(camera: CameraProxy) {
        result = MultiCodeResult.empty

        cancelAllAnim()

        val startAnimTask = {
            val from = if (finderQuad.isZero) {
                normalQuad.copy().apply {
                    scaleByCenter(
                        normalConfig.initialSize / normalConfig.normalSize,
                        normalConfig.initialSize / normalConfig.normalSize
                    )
                }
            } else {
                finderQuad.copy()
            }
            val to = normalQuad.copy()
            singleTransformAnim = createSingleTransformAnim(from, to)
            singleTransformAnim?.doOnEnd {
                if (result.isEmpty) {
                    startZoom()
                }
            }
            singleTransformAnim?.start()
        }

        scanner?.doOnStreaming {
            doOnPreDraw { startAnimTask() }
        } ?: doOnPreDraw { startAnimTask() }
    }

    override fun onFindSuccess(results: List<CodeResult>, bitmap: Bitmap?) {
        doOnLayout {
            result = MultiCodeResult(results.map {
                it.copy().apply { mapFromPercent(previewWidth.toFloat(), previewHeight.toFloat()) }
            })

            invalidate()

            zoomAnim?.cancel()
            zoomAnim = null

            if (result.size == 1) {
                foundSingle()
            } else {
                foundMulti()
            }
        }
    }

    override fun onFindFailure(e: Throwable) {
        if (result.isEmpty) {
            return
        }

        doOnLayout {
            result = MultiCodeResult.empty

            val from = if (finderQuad.isZero) {
                normalQuad.copy().apply {
                    scaleByCenter(
                        normalConfig.initialSize / normalConfig.normalSize,
                        normalConfig.initialSize / normalConfig.normalSize
                    )
                }
            } else {
                finderQuad.copy()
            }
            val to = normalQuad.copy()

            cancelAllAnim()

            if (singleTransformAnim != null) {
                singleTransformAnim!!.setObjectValues(from, to)
            } else {
                singleTransformAnim = createSingleTransformAnim(from, to)
                singleTransformAnim?.start()
            }
        }
    }

    override fun onUnbind() {
        result = MultiCodeResult.empty
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
        quad.copy()
            .apply { scaleByCenter(normalConfig.normalSize, normalConfig.normalSize) }
            .also {
                if (normalQuad != it) {
                    normalQuad = it
                }
            }
        quad.copy()
            .apply { scaleByCenter(normalConfig.initialSize, normalConfig.initialSize) }
            .also {
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
            (height - previewHeight).toFloat() / 2F,
        )
        // drawOutline(canvas)
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
        val to = result.results.first().quad.copy().apply {
            if (isPoint) {
                expand(0.5F)
            } else {
                val l = maxSideLength
                val rect = Quad.zero()
                rect.expand(l / 2F)
                val transform = PerspectiveTransform.quadrilateralToQuadrilateral(rect, this)
                val expandRect = rect.apply {
                    expand(normalConfig.strokeWidth * l * 0.5F + normalConfig.cornerRadius * l * 0.35F)
                }
                transform.transformQuad(expandRect)
                set(expandRect)
            }
        }
        if (singleTransformAnim != null) {
            singleTransformAnim!!.setObjectValues(from, to)
        } else {
            singleTransformAnim = createSingleTransformAnim(from, to)
            singleTransformAnim?.start()
        }
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
        result.results.forEach { result ->
            val quad = result.quad
            canvas.drawLine(quad.lb.x, quad.lb.y, quad.lt.x, quad.lt.y, paint)
            canvas.drawLine(quad.lt.x, quad.lt.y, quad.rt.x, quad.rt.y, paint)
            canvas.drawLine(quad.rt.x, quad.rt.y, quad.rb.x, quad.rb.y, paint)
            canvas.drawLine(quad.rb.x, quad.rb.y, quad.lb.x, quad.lb.y, paint)
        }
    }

    private fun drawResults(canvas: Canvas) {
        canvas.save()

        when (result.size) {
            0 -> {
                drawFinder(canvas)
            }

            1 -> {
                if (isFinderLikeCircle) {
                    val center = finderQuad.center
                    drawPoint(
                        canvas,
                        center,
                        normalConfig.isCircleRadius * normalQuad.minSideLength
                    )
                } else {
                    drawFinder(canvas)
                }
            }

            else -> {
                val f = multiCenterAnim?.animatedFraction ?: 1F
                result.results.forEach { result ->
                    drawPoint(
                        canvas,
                        result.center,
                        f * normalConfig.isCircleRadius * normalQuad.minSideLength
                    )
                }
            }
        }

        canvas.restore()
    }

    private fun drawFinder(canvas: Canvas) {
        canvas.drawPath(finderPath, paint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = if (!result.isEmpty) normalConfig.foundColor else normalConfig.normalColor
            style = Paint.Style.STROKE
            strokeWidth = finderQuad.maxSideLength * kotlin.run {
                val min = max(normalConfig.strokeWidth, 0.1F)
                val max = normalConfig.strokeWidth
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
            color = normalConfig.circleOuterColor
            style = Paint.Style.FILL
            strokeWidth = 0F
        })
        canvas.drawCircle(point.x, point.y, r * 0.618F, paint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = normalConfig.circleInnerColor
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
            scaleByCenter(
                normalConfig.zoomSize / normalConfig.normalSize,
                normalConfig.zoomSize / normalConfig.normalSize
            )
        }
        return ValueAnimator.ofObject(Quad.QuadEvaluator(), from, to).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = normalConfig.zoomDuration
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
            duration = normalConfig.singleTransformDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                finderQuad = animator.animatedValue as Quad
                updateFinder()
            }
            doOnEnd {
                singleTransformAnim = null
                if (result.isEmpty) {
                    startZoom()
                }
            }
        }
    }

    private fun createMultiCenterAnim(): ValueAnimator {
        return ValueAnimator.ofFloat(0F, 1F).apply {
            duration = normalConfig.multiCenterDuration
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
                from.lb.x + normalConfig.cornerRadius + normalConfig.sideLength,
                from.lb.y,
                from.lb.x + normalConfig.cornerRadius,
                from.lb.y,
                from.lb.x,
                from.lb.y,
                from.lb.x,
                from.lb.y + normalConfig.cornerRadius,
                from.lb.x,
                from.lb.y + normalConfig.cornerRadius + normalConfig.sideLength
            )
        )
        finderPath.addCorner(
            floatArrayOf(
                from.lt.x + normalConfig.cornerRadius + normalConfig.sideLength,
                from.lt.y,
                from.lt.x + normalConfig.cornerRadius,
                from.lt.y,
                from.lt.x,
                from.lt.y,
                from.lt.x,
                from.lt.y - normalConfig.cornerRadius,
                from.lt.x,
                from.lt.y - normalConfig.cornerRadius - normalConfig.sideLength
            )
        )
        finderPath.addCorner(
            floatArrayOf(
                from.rt.x - normalConfig.cornerRadius - normalConfig.sideLength,
                from.rt.y,
                from.rt.x - normalConfig.cornerRadius,
                from.rt.y,
                from.rt.x,
                from.rt.y,
                from.rt.x,
                from.rt.y - normalConfig.cornerRadius,
                from.rt.x,
                from.rt.y - normalConfig.cornerRadius - normalConfig.sideLength
            )
        )
        finderPath.addCorner(
            floatArrayOf(
                from.rb.x - normalConfig.cornerRadius - normalConfig.sideLength,
                from.rb.y,
                from.rb.x - normalConfig.cornerRadius,
                from.rb.y,
                from.rb.x,
                from.rb.y,
                from.rb.x,
                from.rb.y + normalConfig.cornerRadius,
                from.rb.x,
                from.rb.y + normalConfig.cornerRadius + normalConfig.sideLength
            )
        )
    }
}
package per.goweii.codex.decorator.finder.wechat

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decoration.ScanDecoration

class WeChatFinderView : View, ScanDecoration {
    private var finderScanLineColor = Color.GREEN
    private var finderScanLineWidthPercent = 1F
    private var finderScanLineHeightPercent = 0.1F
    private var finderScanLineAnimDuration = 3000
    private var finderResultPointRadius = 0F
    private var finderResultPointOuterColor = Color.WHITE
    private var finderResultPointInnerColor = Color.GREEN

    private var finderWidth = 0F
    private var finderHeight = 0F
    private var previewWidth: Int = 0
    private var previewHeight: Int = 0

    private val results = mutableListOf<CodeResult>()
    private val isFound: Boolean
        get() = results.isNotEmpty()
    private val paint: Paint = Paint().apply {
        isAntiAlias = true
    }

    private var animator: ValueAnimator? = null
    private var finderMoveFaction = 0F

    private var finderBitmap: Bitmap? = null
    private var finderSrcRect: Rect = Rect()
    private var finderDstRect: Rect = Rect()

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
        val finderResultPointRadiusDef = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16F,
            context.resources.displayMetrics
        )
        val array = context.obtainStyledAttributes(attrs, R.styleable.WeChatFinderView)
        finderScanLineColor = array.getColor(
            R.styleable.WeChatFinderView_finderScanLineColor,
            finderScanLineColor
        )
        finderScanLineWidthPercent = array.getFraction(
            R.styleable.WeChatFinderView_finderScanLineWidthPercent,
            1, 1,
            finderScanLineWidthPercent
        )
        finderScanLineHeightPercent = array.getFraction(
            R.styleable.WeChatFinderView_finderScanLineHeightPercent,
            1, 1,
            finderScanLineHeightPercent
        )
        finderScanLineAnimDuration = array.getInteger(
            R.styleable.WeChatFinderView_finderScanLineAnimDuration,
            finderScanLineAnimDuration
        )
        finderResultPointRadius = array.getDimension(
            R.styleable.WeChatFinderView_finderResultPointRadius,
            finderResultPointRadiusDef
        )
        finderResultPointOuterColor = array.getColor(
            R.styleable.WeChatFinderView_finderResultPointOuterColor,
            finderResultPointOuterColor
        )
        finderResultPointInnerColor = array.getColor(
            R.styleable.WeChatFinderView_finderResultPointInnerColor,
            finderResultPointInnerColor
        )
        array.recycle()
        if (isInEditMode) {
            finderMoveFaction = 0.5F
        }
    }

    override fun onCreate(scanner: CodeScanner) {
        ratioLiveData = scanner.ratioLiveData
        scanner.ratioLiveData.observeForever(ratioObserver)
    }

    override fun onBind(camera: CameraProxy) {
        results.clear()
        animator?.cancel()
        post {
            animator = createFinderAnim()
            animator?.start()
        }
    }

    override fun onFound(results: List<CodeResult>, bitmap: Bitmap?) {
        animator?.cancel()
        this.results.clear()
        this.results.addAll(results)
        post {
            this.results.forEach {
                it.mapFromPercent(previewWidth.toFloat(), previewHeight.toFloat())
            }
            invalidate()
        }
    }

    override fun onUnbind() {
    }

    override fun onDestroy() {
        ratioLiveData?.removeObserver(ratioObserver)
        ratioLiveData = null
        animator?.cancel()
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
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
        val mw = MeasureSpec.getSize(widthMeasureSpec)
        val mh = MeasureSpec.getSize(heightMeasureSpec)
        val sizeRatio = mh.toFloat() / mw.toFloat()
        if (aspectRatio > sizeRatio) {
            previewWidth = mw
            previewHeight = (mw * aspectRatio).toInt()
        } else if (aspectRatio < sizeRatio) {
            previewWidth = (mh / aspectRatio).toInt()
            previewHeight = mh
        }
        finderWidth = mw.toFloat() * finderScanLineWidthPercent
        finderHeight = finderWidth * finderScanLineHeightPercent
        finderBitmap?.recycle()
        if (finderWidth > 0 && finderHeight > 0) {
            finderBitmap = GradientDrawable().apply {
                gradientType = GradientDrawable.RADIAL_GRADIENT
                colors = intArrayOf(finderScanLineColor, Color.TRANSPARENT)
                gradientRadius = finderWidth / 2F
            }.toBitmap(finderWidth.toInt(), finderWidth.toInt(), Bitmap.Config.ARGB_8888)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 && height <= 0) {
            return
        }
        if (isFound) {
            drawResults(canvas)
        } else {
            drawFinder(canvas)
        }
    }

    private fun drawFinder(canvas: Canvas) {
        val bitmap = finderBitmap ?: return
        if (bitmap.isRecycled) return
        canvas.save()
        val f = finderMoveFaction
        val alphaRange = 0.15F
        val alpha = when {
            f < alphaRange -> f / alphaRange
            f > (1F - alphaRange) -> 1F - ((f - (1F - alphaRange)) / alphaRange)
            else -> 1F
        }
        paint.alpha = (255 * alpha).toInt()
        val alphaColor = ColorUtils.setAlphaComponent(finderScanLineColor, (255 * alpha).toInt())
        paint.apply {
            style = Paint.Style.FILL
            color = alphaColor
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = 0F
        }
        canvas.translate(width.toFloat() / 2F, height.toFloat() / 2F)
        canvas.translate(0F, -width.toFloat() / 2F)
        canvas.translate(0F, width.toFloat() * f)
        canvas.translate(-finderWidth / 2F, -finderHeight / 2F)
        finderSrcRect.set(0, 0, bitmap.width, (bitmap.height * 0.37F).toInt())
        finderDstRect.set(0, 0, finderWidth.toInt(), finderHeight.toInt())
        canvas.drawBitmap(bitmap, finderSrcRect, finderDstRect, paint)
        canvas.restore()
    }

    private fun drawResults(canvas: Canvas) {
        canvas.save()
        val dx = (width - previewWidth).toFloat() / 2F
        val dy = (height - previewHeight).toFloat() / 2F
        canvas.translate(dx, dy)
        paint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            style = Paint.Style.FILL
            strokeWidth = 0F
        }
        results.forEach { result ->
            val point = result.center
            paint.color = finderResultPointOuterColor
            canvas.drawCircle(point.x, point.y, finderResultPointRadius, paint)
            paint.color = finderResultPointInnerColor
            canvas.drawCircle(point.x, point.y, finderResultPointRadius * 0.618F, paint)
        }
        canvas.restore()
    }

    private fun updateFinder() {
        invalidate()
    }

    private fun createFinderAnim(): ValueAnimator {
        return ValueAnimator.ofFloat(0F, 1F).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            duration = finderScanLineAnimDuration.toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                finderMoveFaction = animator.animatedValue as Float
                updateFinder()
            }
            doOnEnd { animator = null }
        }
    }
}
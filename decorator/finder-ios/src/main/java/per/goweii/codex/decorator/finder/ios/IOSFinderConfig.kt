package per.goweii.codex.decorator.finder.ios

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.res.use

data class IOSFinderConfig(
    @ColorInt
    var normalColor: Int = Color.WHITE,
    @ColorInt
    var foundColor: Int = Color.GREEN,
    @ColorInt
    var circleOuterColor: Int = Color.WHITE,
    @ColorInt
    var circleInnerColor: Int = Color.GREEN,
    @FloatRange(from = 0.0, to = 1.0)
    var initialSize: Float = 0.8F,
    @FloatRange(from = 0.0, to = 1.0)
    var normalSize: Float = 0.6F,
    @FloatRange(from = 0.0, to = 1.0)
    var zoomSize: Float = 0.7F,
    @FloatRange(from = 0.0, to = 0.5)
    var strokeWidth: Float = 0.03F,
    @FloatRange(from = 0.0, to = 0.5)
    var cornerRadius: Float = 0.14F,
    @FloatRange(from = 0.0, to = 0.5)
    var sideLength: Float = 0.02F,
    @FloatRange(from = 0.0, to = 0.5)
    var isCircleRadius: Float = 0.0618F,
    @IntRange(from = 0)
    var zoomDuration: Long = 1000L,
    @IntRange(from = 0)
    var singleTransformDuration: Long = 500L,
    @IntRange(from = 0)
    var multiCenterDuration: Long = 500L,
) {
    fun copyFrom(other: IOSFinderConfig) {
        normalColor = other.normalColor
        foundColor = other.foundColor
        circleOuterColor = other.circleOuterColor
        circleInnerColor = other.circleInnerColor
        initialSize = other.initialSize
        normalSize = other.normalSize
        zoomSize = other.zoomSize
        strokeWidth = other.strokeWidth
        cornerRadius = other.cornerRadius
        sideLength = other.sideLength
        isCircleRadius = other.isCircleRadius
        zoomDuration = other.zoomDuration
        singleTransformDuration = other.singleTransformDuration
        multiCenterDuration = other.multiCenterDuration
    }

    companion object {
        fun fromAttr(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
        ): IOSFinderConfig = IOSFinderConfig().apply {
            context.obtainStyledAttributes(attrs, R.styleable.IOSFinderView, defStyleAttr, 0).use {
                normalColor = it.getColor(
                    R.styleable.IOSFinderView_finderNormalColor,
                    normalColor
                )
                foundColor = it.getColor(
                    R.styleable.IOSFinderView_finderFoundColor,
                    foundColor
                )
                circleOuterColor = it.getColor(
                    R.styleable.IOSFinderView_finderCircleOuterColor,
                    circleOuterColor
                )
                circleInnerColor = it.getColor(
                    R.styleable.IOSFinderView_finderCircleInnerColor,
                    circleInnerColor
                )
                initialSize = it.getFraction(
                    R.styleable.IOSFinderView_finderInitSize,
                    1, 1,
                    initialSize
                )
                normalSize = it.getFraction(
                    R.styleable.IOSFinderView_finderNormalSize,
                    1, 1,
                    normalSize
                )
                zoomSize = it.getFraction(
                    R.styleable.IOSFinderView_finderZoomSize,
                    1, 1,
                    zoomSize
                )
                strokeWidth = it.getFraction(
                    R.styleable.IOSFinderView_finderStrokeWidth,
                    1, 1,
                    strokeWidth
                )
                cornerRadius = it.getFraction(
                    R.styleable.IOSFinderView_finderCornerRadius,
                    1, 1,
                    cornerRadius
                )
                sideLength = it.getFraction(
                    R.styleable.IOSFinderView_finderSideLength,
                    1, 1,
                    sideLength
                )
                isCircleRadius = it.getFraction(
                    R.styleable.IOSFinderView_finderIsCircleRadius,
                    1, 1,
                    isCircleRadius
                )
                zoomDuration = it.getInteger(
                    R.styleable.IOSFinderView_finderZoomDuration,
                    zoomDuration.toInt()
                ).toLong()
                singleTransformDuration = it.getInteger(
                    R.styleable.IOSFinderView_finderSingleTransformDuration,
                    singleTransformDuration.toInt()
                ).toLong()
                multiCenterDuration = it.getInteger(
                    R.styleable.IOSFinderView_finderMultiCenterDuration,
                    multiCenterDuration.toInt()
                ).toLong()
            }
        }
    }
}
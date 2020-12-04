package per.goweii.codex

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author CuiZhen
 * @date 2020/10/24
 */
data class Point(
    var x: Float,
    var y: Float
) {
    val isZero: Boolean
        get() = (x == 0F && y == 0F)

    @Suppress("ConvertTwoComparisonsToRangeCheck")
    val isMaybePercent: Boolean
        get() = (x >= 0F && x <= 1F && y >= 0F && y <= 1F)

    fun copy(): Point = Point(x, y)

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun set(p: Point) {
        set(p.x, p.y)
    }

    fun offset(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun offset(p: Point) {
        offset(p.x, p.y)
    }

    fun scale(sx: Float, sy: Float) {
        x *= sx
        y *= sy
    }

    fun mapToPercent(w: Float, h: Float) {
        val sx = if (w <= 0) 0F else 1F / w
        val sy = if (h <= 0) 0F else 1F / h
        scale(sx, sy)
    }

    fun mapFromPercent(w: Float, h: Float) {
        val sx = if (w <= 0) 0F else w
        val sy = if (h <= 0) 0F else h
        scale(sx, sy)
    }

    fun rotate90(w: Float, h: Float) {
        set(h - y, x)
    }

    fun alignTo(from: Point, to: Point, f: Float) {
        set(from.x + (to.x - from.x) * f, from.y + (to.y - from.y) * f)
    }

    fun angleTo(c: Point): Float {
        val angle = atan2((y - c.y), (x - c.x)) * (180.0 / Math.PI).toFloat()
        return if (angle > 0F) angle else 360F + angle
    }

    fun length(p: Point): Float {
        return sqrt(
            abs(x - p.x).toDouble().pow(2.0) + abs(y - p.y).toDouble().pow(2.0)
        ).toFloat()
    }

    override fun toString(): String {
        return "[$x,$y]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Point
        if (x != other.x) return false
        if (y != other.y) return false
        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    companion object {
        fun zero() = Point(0F, 0F)
    }
}

package per.goweii.codex

import android.animation.TypeEvaluator
import kotlin.math.max
import kotlin.math.min

data class Quad(
    val lb: Point,
    val lt: Point,
    val rt: Point,
    val rb: Point
) {
    val points = mutableListOf(lb, lt, rt, rb)
        get() = field.apply { sortBy { it.angleTo(center) } }
    val centerX
        get() = (lb.x + lt.x + rt.x + rb.x) / 4F
    val centerY
        get() = (lb.y + lt.y + rt.y + rb.y) / 4F
    val center = Point.zero()
        get() = field.apply { set(centerX, centerY) }
    val topSideLength: Float
        get() = lt.length(rt)
    val bottomSideLength: Float
        get() = lb.length(rb)
    val leftSideLength: Float
        get() = lt.length(lb)
    val rightSideLength: Float
        get() = rt.length(rb)
    val minSideLength: Float
        get() = min(
            min(topSideLength, bottomSideLength),
            min(leftSideLength, rightSideLength)
        )
    val maxSideLength: Float
        get() = max(
            max(topSideLength, bottomSideLength),
            max(leftSideLength, rightSideLength)
        )

    val isZero: Boolean
        get() = lb.isZero && lt.isZero && rt.isZero && rb.isZero
    val isPoint: Boolean
        get() = lb == lt && lb == rt && lb == rb
    val isMaybePercent: Boolean
        get() = lb.isMaybePercent && lt.isMaybePercent && rt.isMaybePercent && rb.isMaybePercent

    fun set(quad: Quad) {
        lb.set(quad.lb)
        lt.set(quad.lt)
        rt.set(quad.rt)
        rb.set(quad.rb)
    }

    fun copy(): Quad {
        return Quad(
            lb = lb.copy(),
            lt = lt.copy(),
            rt = rt.copy(),
            rb = rb.copy()
        )
    }

    fun offset(dx: Float, dy: Float) {
        lb.offset(dx, dy)
        lt.offset(dx, dy)
        rt.offset(dx, dy)
        rb.offset(dx, dy)
    }

    fun offset(p: Point) {
        offset(p.x, p.y)
    }

    fun offsetToCenter() {
        val c = center
        offset(-c.x, -c.y)
    }

    fun expand(d: Float) {
        lb.offset(-d, -d)
        lt.offset(-d, d)
        rt.offset(d, d)
        rb.offset(d, -d)
    }

    fun scale(sx: Float, sy: Float) {
        lb.scale(sx, sy)
        lt.scale(sx, sy)
        rt.scale(sx, sy)
        rb.scale(sx, sy)
    }

    fun scaleByCenter(sx: Float, sy: Float) {
        val cx = centerX
        val cy = centerY
        offsetToCenter()
        scale(sx, sy)
        offset(cx, cy)
    }

    fun mapToPercent(w: Float, h: Float) {
        lb.mapToPercent(w, h)
        lt.mapToPercent(w, h)
        rt.mapToPercent(w, h)
        rb.mapToPercent(w, h)
    }

    fun mapFromPercent(w: Float, h: Float) {
        lb.mapFromPercent(w, h)
        lt.mapFromPercent(w, h)
        rt.mapFromPercent(w, h)
        rb.mapFromPercent(w, h)
    }

    fun rotate90(w: Float, h: Float, swapCorner: Boolean = false) {
        lb.rotate90(w, h)
        lt.rotate90(w, h)
        rt.rotate90(w, h)
        rb.rotate90(w, h)
        if (swapCorner) {
            val rbx = rb.x
            val rby = rb.y
            rb.set(rt)
            rt.set(lt)
            lt.set(lb)
            lb.set(rbx, rby)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Quad
        if (lb != other.lb) return false
        if (lt != other.lt) return false
        if (rt != other.rt) return false
        if (rb != other.rb) return false
        return true
    }

    override fun hashCode(): Int {
        var result = lb.hashCode()
        result = 31 * result + lt.hashCode()
        result = 31 * result + rt.hashCode()
        result = 31 * result + rb.hashCode()
        return result
    }

    companion object {
        fun zero(): Quad {
            return Quad(
                Point.zero(),
                Point.zero(),
                Point.zero(),
                Point.zero()
            )
        }

        fun point(x: Float, y: Float): Quad {
            return Quad(
                Point(x, y),
                Point(x, y),
                Point(x, y),
                Point(x, y)
            )
        }

        fun point(p: Point): Quad {
            return Quad(
                p.copy(),
                p.copy(),
                p.copy(),
                p.copy()
            )
        }
    }

    class QuadEvaluator(
        private val reuse: Quad = zero()
    ) : TypeEvaluator<Quad> {
        override fun evaluate(f: Float, from: Quad, to: Quad): Quad {
            return reuse.apply {
                val points = points
                val fromPoints = from.points
                val toPoints = to.points
                points[0].alignTo(fromPoints[0], toPoints[0], f)
                points[1].alignTo(fromPoints[1], toPoints[1], f)
                points[2].alignTo(fromPoints[2], toPoints[2], f)
                points[3].alignTo(fromPoints[3], toPoints[3], f)
            }
        }
    }
}
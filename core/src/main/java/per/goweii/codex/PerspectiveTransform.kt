package per.goweii.codex

class PerspectiveTransform(
    private val a11: Float,
    private val a21: Float,
    private val a31: Float,
    private val a12: Float,
    private val a22: Float,
    private val a32: Float,
    private val a13: Float,
    private val a23: Float,
    private val a33: Float
) {
    companion object {
        fun quadrilateralToQuadrilateral(from: Quad, to: Quad): PerspectiveTransform {
            return quadrilateralToQuadrilateral(
                from.lb.x, from.lb.y,
                from.lt.x, from.lt.y,
                from.rt.x, from.rt.y,
                from.rb.x, from.rb.y,
                to.lb.x, to.lb.y,
                to.lt.x, to.lt.y,
                to.rt.x, to.rt.y,
                to.rb.x, to.rb.y
            )
        }

        fun quadrilateralToQuadrilateral(
            x0: Float, y0: Float,
            x1: Float, y1: Float,
            x2: Float, y2: Float,
            x3: Float, y3: Float,
            x0p: Float, y0p: Float,
            x1p: Float, y1p: Float,
            x2p: Float, y2p: Float,
            x3p: Float, y3p: Float
        ): PerspectiveTransform {
            val qToS: PerspectiveTransform =
                quadrilateralToSquare(x0, y0, x1, y1, x2, y2, x3, y3)
            val sToQ: PerspectiveTransform =
                squareToQuadrilateral(x0p, y0p, x1p, y1p, x2p, y2p, x3p, y3p)
            return sToQ.times(qToS)
        }

        fun squareToQuadrilateral(
            x0: Float, y0: Float,
            x1: Float, y1: Float,
            x2: Float, y2: Float,
            x3: Float, y3: Float
        ): PerspectiveTransform {
            val dx3 = x0 - x1 + x2 - x3
            val dy3 = y0 - y1 + y2 - y3
            return if (dx3 == 0.0f && dy3 == 0.0f) {
                PerspectiveTransform(
                    x1 - x0, x2 - x1, x0,
                    y1 - y0, y2 - y1, y0,
                    0.0f, 0.0f, 1.0f
                )
            } else {
                val dx1 = x1 - x2
                val dx2 = x3 - x2
                val dy1 = y1 - y2
                val dy2 = y3 - y2
                val denominator = dx1 * dy2 - dx2 * dy1
                val a13 = (dx3 * dy2 - dx2 * dy3) / denominator
                val a23 = (dx1 * dy3 - dx3 * dy1) / denominator
                PerspectiveTransform(
                    x1 - x0 + a13 * x1, x3 - x0 + a23 * x3, x0,
                    y1 - y0 + a13 * y1, y3 - y0 + a23 * y3, y0,
                    a13, a23, 1.0f
                )
            }
        }

        fun quadrilateralToSquare(
            x0: Float, y0: Float,
            x1: Float, y1: Float,
            x2: Float, y2: Float,
            x3: Float, y3: Float
        ): PerspectiveTransform {
            return squareToQuadrilateral(x0, y0, x1, y1, x2, y2, x3, y3).buildAdjoint()
        }
    }

    fun transformQuad(quad: Quad) {
        val ps = FloatArray(quad.points.size * 2)
        val points = quad.points
        points.forEachIndexed { i, p ->
            ps[i * 2 + 0] = p.x
            ps[i * 2 + 1] = p.y
        }
        transformPoints(ps)
        points.forEachIndexed { i, p ->
            p.set(ps[i * 2 + 0], ps[i * 2 + 1])
        }
    }

    fun transformPoint(point: Point) {
        val ps = floatArrayOf(point.x, point.y)
        transformPoints(ps)
        point.set(ps[0], ps[1])
    }

    fun transformPoints(points: FloatArray) {
        val a11 = a11
        val a12 = a12
        val a13 = a13
        val a21 = a21
        val a22 = a22
        val a23 = a23
        val a31 = a31
        val a32 = a32
        val a33 = a33
        val maxI = points.size - 1 // points.length must be even
        var i = 0
        while (i < maxI) {
            val x = points[i]
            val y = points[i + 1]
            val denominator = a13 * x + a23 * y + a33
            points[i] = (a11 * x + a21 * y + a31) / denominator
            points[i + 1] = (a12 * x + a22 * y + a32) / denominator
            i += 2
        }
    }

    fun transformPoints(xValues: FloatArray, yValues: FloatArray) {
        val n = xValues.size
        for (i in 0 until n) {
            val x = xValues[i]
            val y = yValues[i]
            val denominator = a13 * x + a23 * y + a33
            xValues[i] = (a11 * x + a21 * y + a31) / denominator
            yValues[i] = (a12 * x + a22 * y + a32) / denominator
        }
    }

    private fun buildAdjoint(): PerspectiveTransform {
        return PerspectiveTransform(
            a22 * a33 - a23 * a32,
            a23 * a31 - a21 * a33,
            a21 * a32 - a22 * a31,
            a13 * a32 - a12 * a33,
            a11 * a33 - a13 * a31,
            a12 * a31 - a11 * a32,
            a12 * a23 - a13 * a22,
            a13 * a21 - a11 * a23,
            a11 * a22 - a12 * a21
        )
    }

    private operator fun times(other: PerspectiveTransform): PerspectiveTransform {
        return PerspectiveTransform(
            a11 * other.a11 + a21 * other.a12 + a31 * other.a13,
            a11 * other.a21 + a21 * other.a22 + a31 * other.a23,
            a11 * other.a31 + a21 * other.a32 + a31 * other.a33,
            a12 * other.a11 + a22 * other.a12 + a32 * other.a13,
            a12 * other.a21 + a22 * other.a22 + a32 * other.a23,
            a12 * other.a31 + a22 * other.a32 + a32 * other.a33,
            a13 * other.a11 + a23 * other.a12 + a33 * other.a13,
            a13 * other.a21 + a23 * other.a22 + a33 * other.a23,
            a13 * other.a31 + a23 * other.a32 + a33 * other.a33
        )
    }
}
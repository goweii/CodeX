package per.goweii.codex

class MultiCodeResult(
    results: List<CodeResult>,
) {
    val results: List<CodeResult> = results
        .sortedWith(
            compareBy<CodeResult> { it.format }
                .thenBy { it.text }
        )

    val size: Int get() = results.size

    val isEmpty: Boolean get() = size == 0

    val isNotEmpty: Boolean get() = size > 0

    fun sameTo(other: MultiCodeResult): Boolean {
        if (size != other.size) return false
        val size = size
        for (i in 0 until size) {
            if (!results[i].sameTo(other.results[i])) {
                return false
            }
        }
        return true
    }

    companion object {
        val empty = MultiCodeResult(emptyList())
    }
}

data class CodeResult(
    val format: CodeFormat,
    val text: String,
    val corners: Array<Point>
) {
    val center: Point = Point.zero()
        get() {
            var x = 0F
            var y = 0F
            corners.forEach {
                x += it.x
                y += it.y
            }
            x /= corners.size.toFloat()
            y /= corners.size.toFloat()
            field.set(x, y)
            return field
        }

    val quad: Quad = Quad.zero()
        get() {
            if (corners.size == 4) {
                field.lb.set(corners[0])
                field.lt.set(corners[1])
                field.rt.set(corners[2])
                field.rb.set(corners[3])
            } else {
                val center = center
                field.lb.set(center)
                field.lt.set(center)
                field.rt.set(center)
                field.rb.set(center)
            }
            return field
        }

    val isMaybePercent: Boolean
        get() {
            corners.forEach {
                if (!it.isMaybePercent) {
                    return false
                }
            }
            return true
        }

    fun copy(): CodeResult {
        return CodeResult(
            format,
            text,
            Array(corners.size) { corners[it].copy() }
        )
    }

    fun mapToPercent(w: Float, h: Float) {
        corners.forEach { it.mapToPercent(w, h) }
    }

    fun mapFromPercent(w: Float, h: Float) {
        corners.forEach { it.mapFromPercent(w, h) }
    }

    fun rotate90(w: Float, h: Float) {
        corners.forEach { it.rotate90(w, h) }
    }

    fun rotate180(w: Float, h: Float) {
        rotate90(w, h)
        rotate90(w, h)
    }

    fun rotate270(w: Float, h: Float) {
        rotate90(w, h)
        rotate90(w, h)
        rotate90(w, h)
    }

    fun sameTo(other: CodeResult): Boolean {
        if (format != other.format) return false
        if (text != other.text) return false
        return true
    }

    override fun toString(): String {
        return "format: $format\ntext: $text\ncenter: $center"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CodeResult
        if (format != other.format) return false
        if (text != other.text) return false
        if (!corners.contentEquals(other.corners)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + corners.contentHashCode()
        return result
    }
}
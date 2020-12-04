package per.goweii.codex.processor.mlkit.internal

import com.google.mlkit.vision.barcode.Barcode
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeResult
import per.goweii.codex.Point

internal fun Barcode.toCodeFormat(): CodeFormat {
    return when (format) {
        Barcode.FORMAT_UNKNOWN -> CodeFormat.UNKNOWN
        Barcode.FORMAT_ALL_FORMATS -> CodeFormat.ALL_FORMATS
        Barcode.FORMAT_CODE_128 -> CodeFormat.CODE_128
        Barcode.FORMAT_CODE_39 -> CodeFormat.CODE_39
        Barcode.FORMAT_CODE_93 -> CodeFormat.CODE_93
        Barcode.FORMAT_CODABAR -> CodeFormat.CODABAR
        Barcode.FORMAT_DATA_MATRIX -> CodeFormat.DATA_MATRIX
        Barcode.FORMAT_EAN_13 -> CodeFormat.EAN_13
        Barcode.FORMAT_EAN_8 -> CodeFormat.EAN_8
        Barcode.FORMAT_ITF -> CodeFormat.ITF
        Barcode.FORMAT_QR_CODE -> CodeFormat.QR_CODE
        Barcode.FORMAT_UPC_A -> CodeFormat.UPC_A
        Barcode.FORMAT_UPC_E -> CodeFormat.UPC_E
        Barcode.FORMAT_PDF417 -> CodeFormat.PDF417
        Barcode.FORMAT_AZTEC -> CodeFormat.AZTEC
        else -> throw IllegalStateException()
    }
}

internal fun CodeFormat.toBarcodeFormat(): Int {
    return when (this) {
        CodeFormat.UNKNOWN -> Barcode.FORMAT_UNKNOWN
        CodeFormat.ALL_FORMATS -> Barcode.FORMAT_ALL_FORMATS
        CodeFormat.CODE_128 -> Barcode.FORMAT_CODE_128
        CodeFormat.CODE_39 -> Barcode.FORMAT_CODE_39
        CodeFormat.CODE_93 -> Barcode.FORMAT_CODE_93
        CodeFormat.CODABAR -> Barcode.FORMAT_CODABAR
        CodeFormat.DATA_MATRIX -> Barcode.FORMAT_DATA_MATRIX
        CodeFormat.EAN_13 -> Barcode.FORMAT_EAN_13
        CodeFormat.EAN_8 -> Barcode.FORMAT_EAN_8
        CodeFormat.ITF -> Barcode.FORMAT_ITF
        CodeFormat.QR_CODE -> Barcode.FORMAT_QR_CODE
        CodeFormat.UPC_A -> Barcode.FORMAT_UPC_A
        CodeFormat.UPC_E -> Barcode.FORMAT_UPC_E
        CodeFormat.PDF417 -> Barcode.FORMAT_PDF417
        CodeFormat.AZTEC -> Barcode.FORMAT_AZTEC
    }
}

internal fun Barcode.toCodeResult(): CodeResult {
    val format = toCodeFormat()
    val text = rawValue ?: ""
    val points = cornerPoints
    val corners = Array(points?.size ?: 0) { index ->
        val point = points!![index]
        Point(point.x.toFloat(), point.y.toFloat())
    }
    return CodeResult(format, text, corners)
}
package per.goweii.codex.processor.zxing.internal

import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import per.goweii.codex.CodeErrorCorrectionLevel
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeResult
import per.goweii.codex.Point

internal fun ErrorCorrectionLevel.toCodeErrorCorrectionLevel(): CodeErrorCorrectionLevel {
    return when (this) {
        ErrorCorrectionLevel.L -> CodeErrorCorrectionLevel.L
        ErrorCorrectionLevel.M -> CodeErrorCorrectionLevel.M
        ErrorCorrectionLevel.Q -> CodeErrorCorrectionLevel.Q
        ErrorCorrectionLevel.H -> CodeErrorCorrectionLevel.H
    }
}

internal fun CodeErrorCorrectionLevel.toErrorCorrectionLevel(): ErrorCorrectionLevel {
    return when (this) {
        CodeErrorCorrectionLevel.L -> ErrorCorrectionLevel.L
        CodeErrorCorrectionLevel.M -> ErrorCorrectionLevel.M
        CodeErrorCorrectionLevel.Q -> ErrorCorrectionLevel.Q
        CodeErrorCorrectionLevel.H -> ErrorCorrectionLevel.H
    }
}

internal fun BarcodeFormat.toCodeFormat(): CodeFormat {
    return when (this) {
        BarcodeFormat.CODE_128 -> CodeFormat.CODE_128
        BarcodeFormat.CODE_39 -> CodeFormat.CODE_39
        BarcodeFormat.CODE_93 -> CodeFormat.CODE_93
        BarcodeFormat.CODABAR -> CodeFormat.CODABAR
        BarcodeFormat.DATA_MATRIX -> CodeFormat.DATA_MATRIX
        BarcodeFormat.EAN_13 -> CodeFormat.EAN_13
        BarcodeFormat.EAN_8 -> CodeFormat.EAN_8
        BarcodeFormat.ITF -> CodeFormat.ITF
        BarcodeFormat.QR_CODE -> CodeFormat.QR_CODE
        BarcodeFormat.UPC_A -> CodeFormat.UPC_A
        BarcodeFormat.UPC_E -> CodeFormat.UPC_E
        BarcodeFormat.PDF_417 -> CodeFormat.PDF417
        BarcodeFormat.AZTEC -> CodeFormat.AZTEC
        else -> CodeFormat.UNKNOWN
    }
}

internal fun CodeFormat.toBarcodeFormat(): BarcodeFormat? {
    return when (this) {
        CodeFormat.UNKNOWN -> null
        CodeFormat.ALL_FORMATS -> null
        CodeFormat.CODE_128 -> BarcodeFormat.CODE_128
        CodeFormat.CODE_39 -> BarcodeFormat.CODE_39
        CodeFormat.CODE_93 -> BarcodeFormat.CODE_93
        CodeFormat.CODABAR -> BarcodeFormat.CODABAR
        CodeFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        CodeFormat.EAN_13 -> BarcodeFormat.EAN_13
        CodeFormat.EAN_8 -> BarcodeFormat.EAN_8
        CodeFormat.ITF -> BarcodeFormat.ITF
        CodeFormat.QR_CODE -> BarcodeFormat.QR_CODE
        CodeFormat.UPC_A -> BarcodeFormat.UPC_A
        CodeFormat.UPC_E -> BarcodeFormat.UPC_E
        CodeFormat.PDF417 -> BarcodeFormat.PDF_417
        CodeFormat.AZTEC -> BarcodeFormat.AZTEC
    }
}

internal fun Result.toCodeResult(): CodeResult {
    val format = barcodeFormat.toCodeFormat()
    val text = text ?: ""
    val points = cornerPoints ?: resultPoints
    val corners = Array(points?.size ?: 0) { index ->
        val point = points!![index]
        Point(point.x, point.y)
    }
    return CodeResult(format, text, corners)
}
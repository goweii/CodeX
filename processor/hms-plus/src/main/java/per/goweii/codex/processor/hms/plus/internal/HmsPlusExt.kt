package per.goweii.codex.processor.hms.plus.internal

import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import per.goweii.codex.CodeErrorCorrectionLevel
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeResult
import per.goweii.codex.Point

internal fun HmsBuildBitmapOption.ErrorCorrectionLevel.toCodeErrorCorrectionLevel(): CodeErrorCorrectionLevel {
    return when (this) {
        HmsBuildBitmapOption.ErrorCorrectionLevel.L -> CodeErrorCorrectionLevel.L
        HmsBuildBitmapOption.ErrorCorrectionLevel.M -> CodeErrorCorrectionLevel.M
        HmsBuildBitmapOption.ErrorCorrectionLevel.Q -> CodeErrorCorrectionLevel.Q
        HmsBuildBitmapOption.ErrorCorrectionLevel.H -> CodeErrorCorrectionLevel.H
    }
}

internal fun CodeErrorCorrectionLevel.toErrorCorrectionLevel(): HmsBuildBitmapOption.ErrorCorrectionLevel {
    return when (this) {
        CodeErrorCorrectionLevel.L -> HmsBuildBitmapOption.ErrorCorrectionLevel.L
        CodeErrorCorrectionLevel.M -> HmsBuildBitmapOption.ErrorCorrectionLevel.M
        CodeErrorCorrectionLevel.Q -> HmsBuildBitmapOption.ErrorCorrectionLevel.Q
        CodeErrorCorrectionLevel.H -> HmsBuildBitmapOption.ErrorCorrectionLevel.H
    }
}

internal fun Int.toCodeFormat(): CodeFormat {
    return when (this) {
        HmsScan.FORMAT_UNKNOWN -> CodeFormat.UNKNOWN
        HmsScan.ALL_SCAN_TYPE -> CodeFormat.ALL_FORMATS
        HmsScan.CODE128_SCAN_TYPE -> CodeFormat.CODE_128
        HmsScan.CODE39_SCAN_TYPE -> CodeFormat.CODE_39
        HmsScan.CODE93_SCAN_TYPE -> CodeFormat.CODE_93
        HmsScan.CODABAR_SCAN_TYPE -> CodeFormat.CODABAR
        HmsScan.DATAMATRIX_SCAN_TYPE -> CodeFormat.DATA_MATRIX
        HmsScan.EAN13_SCAN_TYPE -> CodeFormat.EAN_13
        HmsScan.EAN8_SCAN_TYPE -> CodeFormat.EAN_8
        HmsScan.ITF14_SCAN_TYPE -> CodeFormat.ITF
        HmsScan.QRCODE_SCAN_TYPE -> CodeFormat.QR_CODE
        HmsScan.UPCCODE_A_SCAN_TYPE -> CodeFormat.UPC_A
        HmsScan.UPCCODE_E_SCAN_TYPE -> CodeFormat.UPC_E
        HmsScan.PDF417_SCAN_TYPE -> CodeFormat.PDF417
        HmsScan.AZTEC_SCAN_TYPE -> CodeFormat.AZTEC
        else -> throw IllegalStateException()
    }
}

internal fun CodeFormat.toScanType(): Int {
    return when (this) {
        CodeFormat.UNKNOWN -> HmsScan.FORMAT_UNKNOWN
        CodeFormat.ALL_FORMATS -> HmsScan.ALL_SCAN_TYPE
        CodeFormat.CODE_128 -> HmsScan.CODE128_SCAN_TYPE
        CodeFormat.CODE_39 -> HmsScan.CODE39_SCAN_TYPE
        CodeFormat.CODE_93 -> HmsScan.CODE93_SCAN_TYPE
        CodeFormat.CODABAR -> HmsScan.CODABAR_SCAN_TYPE
        CodeFormat.DATA_MATRIX -> HmsScan.DATAMATRIX_SCAN_TYPE
        CodeFormat.EAN_13 -> HmsScan.EAN13_SCAN_TYPE
        CodeFormat.EAN_8 -> HmsScan.EAN8_SCAN_TYPE
        CodeFormat.ITF -> HmsScan.ITF14_SCAN_TYPE
        CodeFormat.QR_CODE -> HmsScan.QRCODE_SCAN_TYPE
        CodeFormat.UPC_A -> HmsScan.UPCCODE_A_SCAN_TYPE
        CodeFormat.UPC_E -> HmsScan.UPCCODE_E_SCAN_TYPE
        CodeFormat.PDF417 -> HmsScan.PDF417_SCAN_TYPE
        CodeFormat.AZTEC -> HmsScan.AZTEC_SCAN_TYPE
    }
}

internal fun HmsScan.toCodeResult(): CodeResult {
    val format = scanType.toCodeFormat()
    val text = originalValue ?: ""
    val points = cornerPoints
    val corners = Array(points?.size ?: 0) { index ->
        val point = points!![index]
        Point(point.x.toFloat(), point.y.toFloat())
    }
    return CodeResult(format, text, corners)
}
package per.goweii.codex.processor.zbar.internal

import com.yanzhenjie.zbar.Symbol
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeResult
import per.goweii.codex.Point

internal fun Int.toCodeFormat(): CodeFormat {
    return when (this) {
        Symbol.CODE128 -> CodeFormat.CODE_128
        Symbol.CODE39 -> CodeFormat.CODE_39
        Symbol.CODE93 -> CodeFormat.CODE_93
        Symbol.CODABAR -> CodeFormat.CODABAR
        Symbol.EAN13 -> CodeFormat.EAN_13
        Symbol.EAN8 -> CodeFormat.EAN_8
        Symbol.QRCODE -> CodeFormat.QR_CODE
        Symbol.UPCA -> CodeFormat.UPC_A
        Symbol.UPCE -> CodeFormat.UPC_E
        Symbol.PDF417 -> CodeFormat.PDF417
        else -> CodeFormat.UNKNOWN
    }
}

internal fun CodeFormat.toSymbolType(): Int {
    return when (this) {
        CodeFormat.CODE_128 -> Symbol.CODE128
        CodeFormat.CODE_39 -> Symbol.CODE39
        CodeFormat.CODE_93 -> Symbol.CODE93
        CodeFormat.CODABAR -> Symbol.CODABAR
        CodeFormat.EAN_13 -> Symbol.EAN13
        CodeFormat.EAN_8 -> Symbol.EAN8
        CodeFormat.QR_CODE -> Symbol.QRCODE
        CodeFormat.UPC_A -> Symbol.UPCA
        CodeFormat.UPC_E -> Symbol.UPCE
        CodeFormat.PDF417 -> Symbol.PDF417
        else -> Symbol.PARTIAL
    }
}

internal fun Symbol.toCodeResult(): CodeResult {
    val format = type.toCodeFormat()
    val text = data ?: ""
    val corners = Array(locationSize) {
        val x = getLocationX(it).toFloat()
        val y = getLocationY(it).toFloat()
        Point(x, y)
    }
    return CodeResult(format, text, corners)
}
package per.goweii.codex.processor.hms.plus

import android.graphics.Bitmap
import androidx.core.util.forEach
import com.huawei.hms.ml.scan.HmsScanAnalyzer
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.huawei.hms.mlsdk.common.MLFrame
import per.goweii.codex.CodeFormat
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.hms.plus.internal.toCodeResult
import per.goweii.codex.processor.hms.plus.internal.toScanType

class HmsPlusDecodeProcessor(
    private val formats: Array<CodeFormat> = arrayOf(CodeFormat.QR_CODE)
) : DecodeProcessor<Bitmap> {
    private val notFountException = CodeNotFoundException
    private val options by lazy {
        HmsScanAnalyzerOptions.Creator().apply {
            val formats = formats.map { it.toScanType() }
            if (formats.isEmpty()) {
                throw RuntimeException("Formats cannot be empty")
            }
            val format0 = formats[0]
            val format1 = mutableListOf<Int>()
            for (i in 1 until formats.size) {
                format1.add(formats[i])
            }
            setHmsScanTypes(format0, *format1.toIntArray())
        }.create()
    }
    private val analyzer by lazy {
        HmsScanAnalyzer(options)
    }

    override fun process(
        input: Bitmap,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = MLFrame.fromBitmap(input)
        val results = analyzer.analyseFrame(image)
        val codeResults = arrayListOf<CodeResult>()
        results.forEach { _, value ->
            if (value.originalValue.isNotEmpty()) {
                codeResults.add(value.toCodeResult())
            }
        }
        if (codeResults.isNullOrEmpty()) {
            onFailure.invoke(notFountException)
        } else {
            onSuccess.invoke(codeResults)
        }
    }
}
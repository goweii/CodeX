package per.goweii.codex.decoder

import android.graphics.Bitmap
import per.goweii.codex.CodeResult

class CodeDecoder(
    private val processor: DecodeProcessor<Bitmap>
) {
    fun decode(
        bitmap: Bitmap,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        processor.process(bitmap, onSuccess, onFailure)
    }
}
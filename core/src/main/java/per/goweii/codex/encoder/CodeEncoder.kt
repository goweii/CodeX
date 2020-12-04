package per.goweii.codex.encoder

import android.graphics.Bitmap

class CodeEncoder(
    private val processor: EncodeProcessor
) {
    fun encode(
        text: String,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        processor.process(text, onSuccess, onFailure)
    }
}
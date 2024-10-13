package per.goweii.codex.encoder

import android.graphics.Bitmap
import per.goweii.codex.CodeProcessor

interface EncodeProcessor : CodeProcessor<String, Bitmap> {
    override fun process(
        input: String,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}
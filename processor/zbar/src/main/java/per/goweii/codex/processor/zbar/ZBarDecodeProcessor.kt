package per.goweii.codex.processor.zbar

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.yanzhenjie.zbar.Config
import com.yanzhenjie.zbar.Image
import com.yanzhenjie.zbar.ImageScanner
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.zbar.internal.toCodeResult

class ZBarDecodeProcessor : DecodeProcessor<Bitmap> {
    private val imageScanner by lazy {
        ImageScanner().apply {
            enableCache(true)
            setConfig(0, Config.X_DENSITY, 3)
            setConfig(0, Config.Y_DENSITY, 3)
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun process(
        input: Bitmap,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val width = input.width
        val height = input.height
        val barcode = Image(width, height, "RGB4")
        val pixels = IntArray(width * height)
        input.getPixels(pixels, 0, width, 0, 0, width, height)
        barcode.setData(pixels)
        val result = imageScanner.scanImage(barcode.convert("Y800"))
        val results = if (result > 0) {
            imageScanner.results.filter {
                it.data.isNotEmpty()
            }.map {
                it.toCodeResult()
            }
        } else null
        if (results.isNullOrEmpty()) {
            onFailure.invoke(CodeNotFoundException)
        } else {
            onSuccess.invoke(results)
        }
    }
}
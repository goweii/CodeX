package per.goweii.codex.processor.zbar

import androidx.camera.core.ImageProxy
import com.yanzhenjie.zbar.Config
import com.yanzhenjie.zbar.Image
import com.yanzhenjie.zbar.ImageScanner
import per.goweii.codex.CodeNotFoundException
import per.goweii.codex.CodeResult
import per.goweii.codex.decoder.DecodeProcessor
import per.goweii.codex.processor.zbar.internal.toCodeResult
import per.goweii.codex.scanner.ImageConverter
import java.util.concurrent.atomic.AtomicReference

class ZBarScanProcessor : DecodeProcessor<ImageProxy> {
    private val imageScanner by lazy {
        ImageScanner().apply {
            enableCache(false)
            setConfig(0, Config.X_DENSITY, 3)
            setConfig(0, Config.Y_DENSITY, 3)
        }
    }
    private val reuseBuffer = AtomicReference<ByteArray?>(null)

    override fun process(
        input: ImageProxy,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val width = input.width
        val height = input.height
        val buffer = ImageConverter.imageToYByteArray(input, reuseBuffer.get())
        val barcode = Image(width, height, "Y800")
        barcode.data = buffer
        val result = imageScanner.scanImage(barcode)
        val results = if (result > 0) {
            imageScanner.results
                .filter { it.data.isNotEmpty() }
                .map { it.toCodeResult() }
        } else null
        if (results.isNullOrEmpty()) {
            reuseBuffer.set(buffer)
            onFailure.invoke(CodeNotFoundException)
        } else {
            onSuccess.invoke(results)
        }
    }
}
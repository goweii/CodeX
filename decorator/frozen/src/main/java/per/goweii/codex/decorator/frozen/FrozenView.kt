package per.goweii.codex.decorator.frozen

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner
import per.goweii.codex.scanner.decorator.ScanDecorator

class FrozenView : AppCompatImageView, ScanDecorator {
    private var scanner: CodeScanner? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        scaleType = ScaleType.CENTER_CROP
    }

    override fun setScaleType(scaleType: ScaleType) {
        super.setScaleType(ScaleType.CENTER_CROP)
    }

    override fun onCreate(scanner: CodeScanner) {
        this.scanner = scanner
    }

    override fun onBind(camera: CameraProxy) {
        scanner?.doOnStreaming {
            setImageBitmap(null)
        } ?: setImageBitmap(null)
    }

    override fun onFindSuccess(results: List<CodeResult>, bitmap: Bitmap?) {
        setImageBitmap(bitmap)
    }

    override fun onFindFailure(e: Throwable) {
    }

    override fun onUnbind() {
    }

    override fun onDestroy() {
        scanner = null
    }
}
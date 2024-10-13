package per.goweii.codex.scanner.decorator

import android.graphics.Bitmap
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner

interface ScanDecorator {
    fun onCreate(scanner: CodeScanner)
    fun onBind(camera: CameraProxy)
    fun onFindSuccess(results: List<CodeResult>, bitmap: Bitmap?)
    fun onFindFailure(e: Throwable)
    fun onUnbind()
    fun onDestroy()
}
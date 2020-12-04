package per.goweii.codex.scanner.decoration

import android.graphics.Bitmap
import per.goweii.codex.CodeResult
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.CodeScanner

interface ScanDecoration {
    fun onCreate(scanner: CodeScanner)
    fun onBind(camera: CameraProxy)
    fun onFound(results: List<CodeResult>, bitmap: Bitmap?)
    fun onUnbind()
    fun onDestroy()
}
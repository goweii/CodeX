package per.goweii.codex.android

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import per.goweii.codex.CodeResult
import per.goweii.codex.MultiCodeResult
import per.goweii.codex.analyzer.luminosity.LuminosityAnalyzer
import per.goweii.codex.android.databinding.ActivitySacnBinding
import per.goweii.codex.android.databinding.IosFinderViewBinding
import per.goweii.codex.android.databinding.WechatFinderViewBinding
import per.goweii.codex.decorator.autozoom.AutoZoomDecorator
import per.goweii.codex.decorator.beep.BeepDecorator
import per.goweii.codex.decorator.finder.ios.IOSFinderView
import per.goweii.codex.decorator.finder.wechat.WeChatFinderView
import per.goweii.codex.decorator.gesture.GestureDecorator
import per.goweii.codex.decorator.vibrate.VibrateDecorator
import per.goweii.codex.processor.hms.plus.HmsPlusScanProcessor
import per.goweii.codex.processor.mlkit.MLKitScanProcessor
import per.goweii.codex.processor.wechat.WeChatQRCodeScanProcessor
import per.goweii.codex.processor.zbar.ZBarScanProcessor
import per.goweii.codex.processor.zxing.ZXingMultiScanProcessor
import per.goweii.codex.processor.zxing.ZXingMultiScanQRCodeProcessor
import per.goweii.codex.processor.zxing.ZXingScanProcessor
import per.goweii.codex.scanner.CameraProxy
import per.goweii.codex.scanner.decorator.ScanDecorator

class ScanActivity : AppCompatActivity() {
    companion object {
        const val PARAMS_PROCESSOR_NAME = "processor_name"
        const val PARAMS_FINDER_VIEW = "finder_view"
        private const val REQ_CAMERA = 1001
        private const val REQ_SETTING = 1002
    }

    private lateinit var binding: ActivitySacnBinding
    private lateinit var finderView: View

    private var result = MultiCodeResult.empty

    private var startTime = 0L

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySacnBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.apply {
            systemUiVisibility = systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        finderView = when (intent.getStringExtra(PARAMS_FINDER_VIEW)) {
            IOSFinderView::class.java.name -> {
                IosFinderViewBinding.inflate(layoutInflater).finderView
            }

            WeChatFinderView::class.java.name -> {
                WechatFinderViewBinding.inflate(layoutInflater).finderView
            }

            else -> throw IllegalArgumentException()
        }
        binding.finderContainer.addView(
            finderView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        val processor = when (intent.getStringExtra(PARAMS_PROCESSOR_NAME)) {
            ZXingScanProcessor::class.java.name -> ZXingScanProcessor()
            ZXingMultiScanProcessor::class.java.name -> ZXingMultiScanProcessor()
            ZXingMultiScanQRCodeProcessor::class.java.name -> ZXingMultiScanQRCodeProcessor()
            ZBarScanProcessor::class.java.name -> ZBarScanProcessor()
            MLKitScanProcessor::class.java.name -> MLKitScanProcessor()
//            HmsScanProcessor::class.java.name -> HmsScanProcessor()
            HmsPlusScanProcessor::class.java.name -> HmsPlusScanProcessor()
            WeChatQRCodeScanProcessor::class.java.name -> WeChatQRCodeScanProcessor(application)
            else -> throw IllegalArgumentException()
        }
        binding.codeScanner.apply {
            addProcessor(processor)
            addAnalyzer(LuminosityAnalyzer {
                if (it < 40.0) {
                    binding.ivTorch.visibility = View.VISIBLE
                } else {
                    if (!binding.ivTorch.isSelected) {
                        binding.ivTorch.visibility = View.GONE
                    }
                }
            })
            addDecorator(
                binding.frozenView,
                finderView as ScanDecorator,
                BeepDecorator(),
                VibrateDecorator(),
                GestureDecorator(),
                AutoZoomDecorator()
            )
            cameraProxyLiveData.observe(this@ScanActivity) { cameraProxy ->
                cameraProxy?.torchState?.observe(this@ScanActivity) { torchState ->
                    when (torchState) {
                        CameraProxy.TORCH_ON -> {
                            binding.ivTorch.isSelected = true
                        }

                        CameraProxy.TORCH_OFF -> {
                            binding.ivTorch.isSelected = false
                        }
                    }
                }
            }
            onFound { r, b -> showResults(r, b) }
            onException { hideResult() }
            bindToLifecycle(this@ScanActivity)
        }
        if (checkSelfPermission()) {
            startScan()
        } else {
            showTip("点击获取相机权限") {
                requestPermission()
            }
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivTorch.setOnClickListener {
            enableTorch(!binding.ivTorch.isSelected)
        }
    }

    private fun enableTorch(enable: Boolean) {
        binding.codeScanner.enableTorch(enable)
    }

    private fun showTip(text: String, click: () -> Unit) {
        binding.tvTip.text = text
        binding.tvTip.visibility = View.VISIBLE
        binding.tvTip.setOnClickListener {
            click.invoke()
        }
    }

    private fun hideTip() {
        binding.tvTip.text = ""
        binding.tvTip.visibility = View.GONE
        binding.tvTip.setOnClickListener(null)
    }

    private fun showResults(results: List<CodeResult>, bitmap: Bitmap?) {
        val lastResult = result
        result = MultiCodeResult(results.map { it.copy() })

        val startTime = this.startTime
        val foundTime = System.currentTimeMillis()
        this.startTime = foundTime

        var centerY = 0F
        val sb = StringBuilder()
        sb.append("扫码用时:${foundTime - startTime}ms")
        results.forEach {
            sb.append("\n")
            sb.append("\n")
            sb.append(it.toString())
            centerY += it.center.y
        }
        binding.tvResult.text = sb.toString()

        if (!lastResult.isEmpty) {
            return
        }

        centerY /= results.size.toFloat()
        centerY *= binding.frozenView.height
        binding.flResult.visibility = View.INVISIBLE
        binding.flResult.post {
            val animList = arrayListOf<Animator>()
            if (bitmap != null) {
                val topHeight = binding.frozenView.height - binding.svResult.height
                var translationY = -(centerY - topHeight / 2F)
                translationY = when {
                    translationY > 0 -> 0F
                    translationY < -binding.svResult.height -> -binding.svResult.height.toFloat()
                    else -> translationY
                }

                animList.add(
                    ObjectAnimator.ofFloat(
                        binding.frozenView,
                        "translationY",
                        binding.frozenView.translationY,
                        translationY
                    )
                )
                animList.add(
                    ObjectAnimator.ofFloat(
                        finderView,
                        "translationY",
                        finderView.translationY,
                        translationY
                    )
                )
            }
            animList.add(
                ObjectAnimator.ofFloat(
                    binding.svResult,
                    "translationY",
                    binding.svResult.height.toFloat(),
                    0F
                )
            )
            AnimatorSet().apply {
                playTogether(animList)
            }.start()
            binding.flResult.visibility = View.VISIBLE
        }
        binding.flResult.setOnClickListener {
            hideResult()
        }
    }

    private fun hideResult() {
        if (result.isEmpty) {
            return
        }

        result = MultiCodeResult.empty

        binding.flResult.setOnClickListener(null)
        val frozenViewAnim = ObjectAnimator.ofFloat(
            binding.frozenView,
            "translationY",
            binding.frozenView.translationY,
            0F
        )
        val finderViewAnim = ObjectAnimator.ofFloat(
            finderView,
            "translationY",
            finderView.translationY,
            0F
        )
        val cardViewAnim = ObjectAnimator.ofFloat(
            binding.svResult,
            "translationY",
            0F,
            binding.svResult.height.toFloat()
        )
        AnimatorSet().apply {
            playTogether(frozenViewAnim, finderViewAnim, cardViewAnim)
            doOnEnd {
                if (result.isEmpty) {
                    binding.flResult.visibility = View.GONE
                }
            }
        }.start()
        startScan()
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        hideTip()
        startTime = System.currentTimeMillis()
        binding.codeScanner.startScan()
    }

    private fun checkSelfPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.CAMERA
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CAMERA
            ), REQ_CAMERA
        )
    }

    private fun gotoSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        try {
            startActivityForResult(intent, REQ_SETTING)
        } catch (e: Exception) {
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CAMERA) {
            if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                startScan()
            } else {
                if (shouldShowRequestPermissionRationale()) {
                    showTip("未获取到相机权限") {
                        requestPermission()
                    }
                } else {
                    showTip("请到设置打开相机权限") {
                        gotoSetting()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SETTING) {
            if (checkSelfPermission()) {
                startScan()
            } else {
                if (shouldShowRequestPermissionRationale()) {
                    showTip("点击获取相机权限") {
                        requestPermission()
                    }
                } else {
                    showTip("请到设置打开相机权限") {
                        gotoSetting()
                    }
                }
            }
        }
    }
}
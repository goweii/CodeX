package per.goweii.codex.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import per.goweii.codex.android.databinding.ActivityMainBinding
import per.goweii.codex.decoration.finder.ios.IOSFinderView
import per.goweii.codex.decoration.finder.wechat.WeChatFinderView
import per.goweii.codex.processor.hms.HmsDecodeProcessor
import per.goweii.codex.processor.hms.HmsEncodeProcessor
import per.goweii.codex.processor.hms.HmsScanProcessor
import per.goweii.codex.processor.mlkit.MLKitDecodeProcessor
import per.goweii.codex.processor.mlkit.MLKitScanProcessor
import per.goweii.codex.processor.zbar.ZBarDecodeProcessor
import per.goweii.codex.processor.zbar.ZBarScanProcessor
import per.goweii.codex.processor.zxing.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var scanFinderIndex = 0
    private val scanFinderList = arrayListOf<Class<*>>().apply {
        add(IOSFinderView::class.java)
        add(WeChatFinderView::class.java)
    }
    private var scanProcessorIndex = 0
    private val scanProcessorList = arrayListOf<Class<*>>().apply {
        add(ZXingScanProcessor::class.java)
        add(ZXingMultiScanProcessor::class.java)
        add(ZXingMultiScanQRCodeProcessor::class.java)
        add(ZBarScanProcessor::class.java)
        add(MLKitScanProcessor::class.java)
        add(HmsScanProcessor::class.java)
//        add(HmsPlusScanProcessor::class.java)
    }
    private var decodeProcessorIndex = 0
    private val decodeProcessorList = arrayListOf<Class<*>>().apply {
        add(ZXingDecodeProcessor::class.java)
        add(ZXingMultiDecodeProcessor::class.java)
        add(ZXingMultiDecodeQRCodeProcessor::class.java)
        add(ZBarDecodeProcessor::class.java)
        add(MLKitDecodeProcessor::class.java)
        add(HmsDecodeProcessor::class.java)
//        add(HmsPlusDecodeProcessor::class.java)
    }
    private var encodeProcessorIndex = 0
    private val encodeProcessorList = arrayListOf<Class<*>>().apply {
        add(ZXingEncodeProcessor::class.java)
        add(ZXingEncodeQRCodeProcessor::class.java)
        add(HmsEncodeProcessor::class.java)
//        add(HmsPlusEncodeProcessor::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setScanFinder()
        setScanProcessor()
        setDecodeProcessor()
        setEncodeProcessor()
        binding.scanFinder.setOnClickListener {
            chooseScanFinder()
        }
        binding.scanProcessor.setOnClickListener {
            chooseScanProcessor()
        }
        binding.decodeProcessor.setOnClickListener {
            chooseDecodeProcessor()
        }
        binding.encodeProcessor.setOnClickListener {
            chooseEncodeProcessor()
        }
        binding.scanner.setOnClickListener {
            startScan()
        }
        binding.decoder.setOnClickListener {
            startDecode()
        }
        binding.encoder.setOnClickListener {
            startEncode()
        }
    }

    private fun chooseScanFinder() {
        val items = scanFinderList.map { it.simpleName }.toTypedArray()
        AlertDialog.Builder(this)
            .setSingleChoiceItems(items, scanFinderIndex) { dialogInterface, i ->
                scanFinderIndex = i
                setScanFinder()
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    private fun chooseScanProcessor() {
        val items = scanProcessorList.map { it.simpleName }.toTypedArray()
        AlertDialog.Builder(this)
            .setSingleChoiceItems(items, scanProcessorIndex) { dialogInterface, i ->
                scanProcessorIndex = i
                setScanProcessor()
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    private fun chooseDecodeProcessor() {
        val items = decodeProcessorList.map { it.simpleName }.toTypedArray()
        AlertDialog.Builder(this)
            .setSingleChoiceItems(items, decodeProcessorIndex) { dialogInterface, i ->
                decodeProcessorIndex = i
                setDecodeProcessor()
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    private fun chooseEncodeProcessor() {
        val items = encodeProcessorList.map { it.simpleName }.toTypedArray()
        AlertDialog.Builder(this)
            .setSingleChoiceItems(items, encodeProcessorIndex) { dialogInterface, i ->
                encodeProcessorIndex = i
                setEncodeProcessor()
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    private fun setScanFinder() {
        binding.scanFinder.text = scanFinderList[scanFinderIndex].simpleName
    }

    private fun setScanProcessor() {
        binding.scanProcessor.text = scanProcessorList[scanProcessorIndex].simpleName
    }

    private fun setDecodeProcessor() {
        binding.decodeProcessor.text = decodeProcessorList[decodeProcessorIndex].simpleName
    }

    private fun setEncodeProcessor() {
        binding.encodeProcessor.text = encodeProcessorList[encodeProcessorIndex].simpleName
    }

    private fun startScan() {
        startActivity(Intent(this, ScanActivity::class.java).apply {
            putExtra(ScanActivity.PARAMS_PROCESSOR_NAME, scanProcessorList[scanProcessorIndex].name)
            putExtra(ScanActivity.PARAMS_FINDER_VIEW, scanFinderList[scanFinderIndex].name)
        })
    }

    private fun startDecode() {
        startActivity(Intent(this, DecodeActivity::class.java).apply {
            putExtra(
                DecodeActivity.PARAMS_PROCESSOR_NAME,
                decodeProcessorList[decodeProcessorIndex].name
            )
        })
    }

    private fun startEncode() {
        startActivity(Intent(this, EncodeActivity::class.java).apply {
            putExtra(
                EncodeActivity.PARAMS_PROCESSOR_NAME,
                encodeProcessorList[encodeProcessorIndex].name
            )
        })
    }
}
package per.goweii.codex.android

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import per.goweii.codex.android.databinding.ActivityDecodeBinding
import per.goweii.codex.decoder.CodeDecoder
import per.goweii.codex.processor.hms.plus.HmsPlusDecodeProcessor
import per.goweii.codex.processor.mlkit.MLKitDecodeProcessor
import per.goweii.codex.processor.zbar.ZBarDecodeProcessor
import per.goweii.codex.processor.zxing.ZXingDecodeProcessor
import per.goweii.codex.processor.zxing.ZXingMultiDecodeProcessor
import per.goweii.codex.processor.zxing.ZXingMultiDecodeQRCodeProcessor

class DecodeActivity : AppCompatActivity() {

    companion object {
        const val PARAMS_PROCESSOR_NAME = "processor_name"
        private const val SELECT_IMAGE = 1001
    }

    private lateinit var binding: ActivityDecodeBinding
    private lateinit var decoder: CodeDecoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val processor = when (intent.getStringExtra(PARAMS_PROCESSOR_NAME)) {
            ZXingDecodeProcessor::class.java.name -> ZXingDecodeProcessor()
            ZXingMultiDecodeProcessor::class.java.name -> ZXingMultiDecodeProcessor()
            ZXingMultiDecodeQRCodeProcessor::class.java.name -> ZXingMultiDecodeQRCodeProcessor()
            ZBarDecodeProcessor::class.java.name -> ZBarDecodeProcessor()
            MLKitDecodeProcessor::class.java.name -> MLKitDecodeProcessor()
//            HmsDecodeProcessor::class.java.name -> HmsDecodeProcessor()
            HmsPlusDecodeProcessor::class.java.name -> HmsPlusDecodeProcessor()
            else -> throw IllegalArgumentException()
        }
        decoder = CodeDecoder(processor)
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
        binding.imageView.setOnClickListener {
            selectImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE) {
            val uri = data?.data ?: return
            contentResolver.openInputStream(uri)?.use {
                val bitmap = BitmapFactory.decodeStream(it)
                binding.imageView.setImageBitmap(bitmap)
                decode()
            }
        }
    }

    private fun decode() {
        val drawable = binding.imageView.drawable
        drawable as BitmapDrawable
        decoder.decode(drawable.bitmap, onSuccess = { results ->
            val sb = StringBuilder()
            sb.append("识别结果：")
            results.forEach {
                sb.append("\n")
                sb.append("\n")
                sb.append(it.toString())
            }
            binding.textView.text = sb.toString()
        }, onFailure = {
            binding.textView.text = "识别失败"
        })
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, SELECT_IMAGE)
    }
}
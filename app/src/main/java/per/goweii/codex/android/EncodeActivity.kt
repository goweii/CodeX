package per.goweii.codex.android

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import per.goweii.codex.CodeErrorCorrectionLevel
import per.goweii.codex.android.databinding.ActivityEncodeBinding
import per.goweii.codex.encoder.CodeEncoder
import per.goweii.codex.processor.hms.plus.HmsPlusEncodeProcessor
import per.goweii.codex.processor.zxing.ZXingEncodeProcessor
import per.goweii.codex.processor.zxing.ZXingEncodeQRCodeProcessor

class EncodeActivity : AppCompatActivity() {

    companion object {
        const val PARAMS_PROCESSOR_NAME = "processor_name"
    }

    private lateinit var binding: ActivityEncodeBinding
    private lateinit var encoder: CodeEncoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
        binding.sbLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prepareEncoder()
                encode()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.sbLevel.max = CodeErrorCorrectionLevel.values().size - 1
        binding.sbLevel.progress = 0
        prepareEncoder()
        binding.editText.doOnTextChanged { text, _, _, _ ->
            encode()
        }
    }

    private fun prepareEncoder() {
        val progress = binding.sbLevel.progress
        val correctionLevel = CodeErrorCorrectionLevel.values()[progress]
        binding.tvLevel.text = "Level(${correctionLevel.name})"
        val processor = when (intent.getStringExtra(PARAMS_PROCESSOR_NAME)) {
            ZXingEncodeProcessor::class.java.name -> ZXingEncodeProcessor(
                errorCorrectionLevel = correctionLevel
            )
//            HmsEncodeProcessor::class.java.name -> HmsEncodeProcessor(
//                errorCorrectionLevel = correctionLevel
//            )
            HmsPlusEncodeProcessor::class.java.name -> HmsPlusEncodeProcessor(
                errorCorrectionLevel = correctionLevel
            )
            ZXingEncodeQRCodeProcessor::class.java.name -> ZXingEncodeQRCodeProcessor(
                errorCorrectionLevel = correctionLevel
            )
            else -> throw IllegalArgumentException()
        }
        encoder = CodeEncoder(processor)
    }

    private fun encode() {
        val text = binding.editText.text.toString()
        encoder.encode(text, onSuccess = {
            binding.imageView.setImageBitmap(it)
        }, onFailure = {
            binding.imageView.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
        })
    }
}
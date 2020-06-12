package com.example.mediademo.audio

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.example.mediademo.R
import com.example.mediademo.databinding.ActivityAudioPlayBinding
import com.example.mediademo.ui.BaseUIActivity
import java.util.*

class AudioPlayActivity : BaseUIActivity<ActivityAudioPlayBinding>() {


    companion object {
        const val INTENT_KEY = "audio"
    }

    private lateinit var audioInfo: AudioInfo
    private lateinit var recordHelper: RecordHelper
    private var handler: MyHandler = MyHandler()


    override fun getLayoutId(): Int {
        return R.layout.activity_audio_play
    }

    override fun init() {
        audioInfo = intent.getParcelableExtra(INTENT_KEY)
        handler = MyHandler()
        recordHelper = RecordHelper.RecordHelperBuilder()
                .withContext(this)
                .withHandler(handler)
                .withRecordPcmPath(audioInfo.path)
                .build()
    }

    override fun initEvent() {
        dataBinding.playStart.setOnClickListener { recordHelper.playInModeStream() }
        dataBinding.playPause.setOnClickListener { recordHelper.playPause() }
        dataBinding.playStop.setOnClickListener { recordHelper.playStop() }
    }

    override fun initData() {
    }

    internal inner class MyHandler : Handler() {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                RecordHelper.SET_PROGRESS -> {
                    val progress = msg.arg1
                    dataBinding.progress.progress = progress
                    dataBinding.progressText.text = "$progress%"
                }
                RecordHelper.RESET_PROGRESS -> {
                    dataBinding.progress.progress = 0
                    dataBinding.progressText.text = "0%"
                }
            }
        }
    }
}

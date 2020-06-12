package com.example.mediademo.audio;

import android.media.MediaExtractor;
import android.os.Environment;

import com.example.mediademo.R;
import com.example.mediademo.databinding.ActivityAudioExtractionBinding;
import com.example.mediademo.ui.BaseUIActivity;

import java.io.File;

/**
 * @description: 从视频中提取音频
 * @author: chenjiayou
 * @createBy: 2020-6-12
 */

public class AudioExtractionActivity extends BaseUIActivity<ActivityAudioExtractionBinding> {
    private String srcPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/video.mp4";
    private String pcmPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/test.acc";
    private String mp4Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/test.h264";

    private MediaExtractor mediaExtractor;


    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_extraction;
    }

    @Override
    public void init() {
        File file = new File(srcPath);
        if (file.exists()) {
            StringBuffer sb = new StringBuffer();
            sb.append("path: ").append(file.getAbsolutePath()).append("\n");
            sb.append("length: ").append(getFileSize(file.length())).append("M");
            getDataBinding().tvVideoInfo.setText(sb.toString());
        }
    }

    public double getFileSize(long length) {
        return (double) length / (double) 1024 * 1024;
    }

    @Override
    public void initEvent() {
        getDataBinding().audioExtraction.setOnClickListener(v -> new AudioHelper().decode());
    }

    @Override
    public void initData() {

    }


}

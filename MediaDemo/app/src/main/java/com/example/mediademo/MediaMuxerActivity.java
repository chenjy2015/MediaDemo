package com.example.mediademo;

import com.example.mediademo.databinding.ActivityMediaMuxerBinding;
import com.example.mediademo.ui.BaseUIActivity;
import com.example.mediademo.util.MediaMuxerUtil;

public class MediaMuxerActivity extends BaseUIActivity<ActivityMediaMuxerBinding> {

    @Override
    public int getLayoutId() {
        return R.layout.activity_media_muxer;
    }

    @Override
    public void init() {

    }

    @Override
    public void initEvent() {
        getDataBinding().extractMedia.setOnClickListener(v -> MediaMuxerUtil.extracMedia());
        getDataBinding().extractAudio.setOnClickListener(v -> MediaMuxerUtil.extracAudio());
        getDataBinding().merge.setOnClickListener(v -> MediaMuxerUtil.combineVideo());
    }

    @Override
    public void initData() {
    }


}

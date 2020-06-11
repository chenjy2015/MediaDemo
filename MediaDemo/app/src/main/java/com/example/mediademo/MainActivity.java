package com.example.mediademo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.PermissionUtils;
import com.example.mediademo.audio.AudioRecord2Activity;
import com.example.mediademo.databinding.ActivityMainBinding;
import com.example.mediademo.opengl.SimpleRender;
import com.example.mediademo.ui.BaseUIActivity;

public class MainActivity extends BaseUIActivity<ActivityMainBinding> {

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("WrongConstant")
    @Override
    public void init() {
        PermissionUtils.permission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied() {

                    }
                }).request();
    }

    @Override
    public void initEvent() {
        getDataBinding().imgPre.setOnClickListener(v -> startActivity(new Intent(this, ImagePreviewActivity.class)));
        getDataBinding().record.setOnClickListener(v -> startActivity(new Intent(this, AudioRecord2Activity.class)));
        getDataBinding().surfacePre.setOnClickListener(v -> startActivity(new Intent(this, VideoActivity.class)));
        getDataBinding().texturePre.setOnClickListener(v -> startActivity(new Intent(this, TextureViewActivity.class)));
        getDataBinding().muxer.setOnClickListener(v -> startActivity(new Intent(this, MediaMuxerActivity.class)));
        getDataBinding().openGlTriangle.setOnClickListener(v -> {
            Intent intent = new Intent(this, SimpleRenderActivity.class);
            intent.putExtra("type", 0);
            startActivity(intent);
        });
        getDataBinding().openGlBitmap.setOnClickListener(v -> {
            Intent intent = new Intent(this, SimpleRenderActivity.class);
            intent.putExtra("type", 1);
            startActivity(intent);
        });

    }

    @Override
    public void initData() {

    }
}

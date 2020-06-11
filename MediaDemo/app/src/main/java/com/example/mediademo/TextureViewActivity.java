package com.example.mediademo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.blankj.utilcode.util.PermissionUtils;
import com.example.mediademo.databinding.ActivityTextureViewBinding;
import com.example.mediademo.ui.BaseUIActivity;

import java.io.IOException;

public class TextureViewActivity extends BaseUIActivity<ActivityTextureViewBinding> implements TextureView.SurfaceTextureListener {
    Camera camera;
    SurfaceHolder surfaceHolder;

    @Override
    public int getLayoutId() {
        return R.layout.activity_texture_view;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void init() {

        PermissionUtils.permission(Manifest.permission.CAMERA).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                getDataBinding().textureView.setSurfaceTextureListener(TextureViewActivity.this);// 打开摄像头并将展示方向旋转90度
                // 打开摄像头并将展示方向旋转90度
                camera = Camera.open();
                camera.setDisplayOrientation(90);
            }

            @Override
            public void onDenied() {

            }
        }).request();
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            camera.setPreviewTexture(surface);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}

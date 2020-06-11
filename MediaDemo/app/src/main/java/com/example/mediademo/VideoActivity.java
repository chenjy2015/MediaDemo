package com.example.mediademo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.example.mediademo.databinding.ActivityVideoBinding;
import com.example.mediademo.ui.BaseUIActivity;

import java.io.IOException;

public class VideoActivity extends BaseUIActivity<ActivityVideoBinding> implements SurfaceHolder.Callback2 , Camera.PreviewCallback {

    Camera camera;
    SurfaceHolder surfaceHolder;

    @Override
    public int getLayoutId() {
        return R.layout.activity_video;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void init() {

        PermissionUtils.permission(Manifest.permission.CAMERA).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                surfaceHolder = getDataBinding().surfaceView.getHolder();
                surfaceHolder.addCallback(VideoActivity.this);

                // 打开摄像头并将展示方向旋转90度
                camera = Camera.open();
                Camera.Parameters parameters =  camera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);
                camera.setPreviewCallback(VideoActivity.this::onPreviewFrame);
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
    public void surfaceRedrawNeeded(SurfaceHolder holder) {


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.release();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        LogUtils.d("VideoActivity",data);
    }
}

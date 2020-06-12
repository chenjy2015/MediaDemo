package com.example.mediademo.audio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.mediademo.R;
import com.example.mediademo.databinding.ActivityRecordBinding;
import com.example.mediademo.ui.BaseUIActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @description: 录音与播放
 * @author: chenjiayou
 * @createBy: 2020-6-12
 */

public class AudioRecord2Activity extends BaseUIActivity<ActivityRecordBinding> {

    RecordHelper recordHelper;
    private MyHandler handler = new MyHandler();
    private Timer timer;


    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_record;
    }

    @Override
    public void init() {
        recordHelper = new RecordHelper.RecordHelperBuilder().withContext(this).withHandler(handler).build();
    }

    @Override
    public void initEvent() {
        getDataBinding().recordStart.setOnClickListener(v -> recordHelper.startRecord());
        getDataBinding().recordStop.setOnClickListener(v -> recordHelper.stopRecord());
        getDataBinding().playStart.setOnClickListener(v -> recordHelper.playInModeStream());
        getDataBinding().playPause.setOnClickListener(v -> recordHelper.playPause());
        getDataBinding().playStop.setOnClickListener(v -> recordHelper.playStop());
        getDataBinding().play.setOnClickListener(v -> {
            Intent intent = new Intent(AudioRecord2Activity.this, AudioPlayActivity.class);
            intent.putExtra(AudioPlayActivity.INTENT_KEY, new AudioInfo(recordHelper.getDefaultRecordPcmPath(), 0));
            startActivity(intent);
        });
    }

    @Override
    public void initData() {
        PermissionUtils
                .permission(permissions)
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {

                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < permissionsDenied.size(); i++) {
                            sb.append(permissionsDenied.get(i));
                            if (i < permissionsDenied.size() - 1) {
                                sb.append(" , ");
                            }
                        }
                        ToastUtils.showShort("权限" + sb.toString() + "被拒绝!");
                    }

                }).request();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordHelper.stopRecord();
        recordHelper.playStop();
    }

    /**
     * 录音计时器开始
     */
    public void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }
        final int[] duration = {0};
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                duration[0]++;
                Message msg = Message.obtain();
                msg.what = RecordHelper.RECORD_PROGRESS;
                msg.arg1 = duration[0];
                handler.sendMessage(msg);
            }
        }, 1000, 1000);
    }

    /**
     * 录音计时器停止
     */
    public void stopTimer() {
        timer.cancel();
        timer = null;
    }


    class MyHandler extends Handler {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RecordHelper.SET_PROGRESS:
                    int progress = msg.arg1;
                    getDataBinding().progress.setProgress(progress);
                    getDataBinding().progressText.setText(progress + "%");
                    break;
                case RecordHelper.RESET_PROGRESS:
                    getDataBinding().progress.setProgress(0);
                    getDataBinding().progressText.setText("0%");
                    break;
                case RecordHelper.RECORD_START:
                    startTimer();
                    break;
                case RecordHelper.RECORD_PROGRESS:
                    getDataBinding().recordText.setText(msg.arg1 + "");
                    break;
                case RecordHelper.RECORD_STOP:
                    getDataBinding().recordText.setText("");
                    stopTimer();
                    break;
            }
        }
    }
}

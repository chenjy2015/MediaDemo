package com.example.mediademo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.mediademo.config.GlobalConfig;
import com.example.mediademo.databinding.ActivityAudioRecordBinding;
import com.example.mediademo.ui.BaseUIActivity;
import com.example.mediademo.util.PcmToWavUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AudioRecordActivity extends BaseUIActivity<ActivityAudioRecordBinding> {

    private static final String TAG = "jqd";
    private AudioRecord audioRecord; // 声明 AudioRecord 对象
    private AudioTrack audioTrack; // 声明 AudioTrack 播放音频对象
    private boolean isRecording; // 标示当前是否正在录音


    /**
     * 需要申请的运行时权限
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_record;
    }


    @Override
    public void init() {

    }

    @Override
    public void initEvent() {

        getDataBinding().record.setOnClickListener(v -> {
            String text = getDataBinding().record.getText().toString();
            if ("开始录音".equals(text)) {
                getDataBinding().record.setText("停止录音");
                startRecord();
            } else {
                getDataBinding().record.setText("开始录音");
                stopRecord();
            }
        });

        getDataBinding().convert.setOnClickListener(v -> {
            PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(GlobalConfig.SAMPLE_RATE_INHZ, GlobalConfig.CHANNEL_CONFIG, GlobalConfig.AUDIO_FORMAT);
            File pcmFile = getRecordPcm();
            File wavFile = getRecordWav();
            if (!wavFile.mkdirs()) {
                Log.e(TAG, "wavFile Directory not created");
            }
            if (wavFile.exists()) {
                wavFile.delete();
            }
            pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
        });

        getDataBinding().action.setOnClickListener(v -> {
            String text = getDataBinding().action.getText().toString();
            if ("播放".equals(text)) {
                playInModeStream();
                getDataBinding().action.setText("停止");
            } else {
                stopPlay();
                getDataBinding().action.setText("播放");
            }
        });
    }

    @Override
    public void initData() {
        checkPermission();
    }

    public void checkPermission() {
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

    public File getRecordPcm() {
        return new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), GlobalConfig.PCM_FILE_NAME);
    }

    public File getRecordWav() {
        return new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), GlobalConfig.WAV_FILE_NAME);
    }

    public void startRecord() {
        final int minBufferSize = AudioRecord.getMinBufferSize(
                GlobalConfig.SAMPLE_RATE_INHZ,
                GlobalConfig.CHANNEL_CONFIG,
                GlobalConfig.AUDIO_FORMAT);

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                GlobalConfig.SAMPLE_RATE_INHZ,
                GlobalConfig.CHANNEL_CONFIG,
                GlobalConfig.AUDIO_FORMAT,
                minBufferSize);


        //初始化文件
        File file = getRecordPcm();
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        if (file.exists()) {
            file.delete();
        }

        audioRecord.startRecording();
        isRecording = true;
        byte[] data = new byte[minBufferSize];


        // todo pcm数据无法直接播放，保存为WAV格式。
        new Thread(() -> {
            FileOutputStream fos = null;
            try {
                //初始化要输出的目标录音文件 xx.pcm
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (null != fos) {
                while (isRecording) {
                    int read = audioRecord.read(data, 0, minBufferSize);
                    // 如果读取音频数据没有出现错误，就将数据写入到文件
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            fos.write(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Log.i(TAG, "run: close file output stream !");
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }

    public void stopRecord() {
        isRecording = false;
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }


    /**
     * 播放，使用stream模式
     */
    @SuppressLint("InlinedApi")
    public void playInModeStream() {
        int minBufferSize = AudioTrack.getMinBufferSize(GlobalConfig.SAMPLE_RATE_INHZ, AudioFormat.CHANNEL_OUT_MONO, GlobalConfig.AUDIO_FORMAT);
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(AudioFormat.SAMPLE_RATE_UNSPECIFIED)
                        .setEncoding(GlobalConfig.AUDIO_FORMAT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
        );
        //播放动作开始
        audioTrack.play();
        //开始读取临时文件 xx.pcm 然后播放语音
        File file = getRecordPcm();

        new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(file);
                if (null != fis) {
                    byte[] tempBuffer = new byte[minBufferSize];
                    while (fis.available() > 0) {
                        int readCount = fis.read(tempBuffer);
                        if (readCount == AudioTrack.ERROR_BAD_VALUE || readCount == AudioTrack.ERROR_INVALID_OPERATION) {
                            continue;
                        }
                        if (readCount != 0 && readCount != -1) {
                            audioTrack.write(tempBuffer, 0, readCount);
                        }
                    }
                    fis.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 播放，使用static模式
     */
    @SuppressLint("StaticFieldLeak")
    private void playInModeStatic() {
        // static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区
        new AsyncTask<Void, Void, byte[]>() {

            @Override
            protected byte[] doInBackground(Void... voids) {
                try {
                    //读取 文件 并写入字节数组中 返回
                    InputStream in = getResources().openRawResource(R.raw.ding);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        for (int b; (b = in.read()) != -1; ) {
                            bos.write(b);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        in.close();
                        bos.close();
                    }
                    Log.d(TAG, "Got the data");
                    return bos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.wtf(TAG, "Failed to read", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(byte[] bytes) {
                super.onPostExecute(bytes);
                Log.i(TAG, "Creating track...audioData.length = " + bytes.length);

                // R.raw.ding铃声文件的相关属性为 22050Hz, 8-bit, Mono
                audioTrack = new AudioTrack(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build(),
                        new AudioFormat.Builder().setSampleRate(22050)
                                .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build(),
                        bytes.length,
                        AudioTrack.MODE_STATIC,
                        AudioManager.AUDIO_SESSION_ID_GENERATE);
                Log.d(TAG, "Writing audio data...");
                audioTrack.write(bytes, 0, bytes.length);
                Log.d(TAG, "Starting playback");
                audioTrack.play();
                Log.d(TAG, "Playing");
            }
        }.execute();
    }

    /**
     * 停止播放
     */
    private void stopPlay() {
        if (audioTrack != null) {
            Log.d(TAG, "Stopping");
            audioTrack.stop();
            Log.d(TAG, "Releasing");
            audioTrack.release();
            Log.d(TAG, "Nulling");
        }
    }
}

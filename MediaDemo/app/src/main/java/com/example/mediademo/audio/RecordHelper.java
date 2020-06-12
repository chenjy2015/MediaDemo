package com.example.mediademo.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.example.mediademo.config.GlobalConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @description: 录音与播放音频辅助类
 * @author: chenjiayou
 * @createBy: 2020-6-12
 */

public class RecordHelper implements IRecord {

    public static final String TAG = "audio_record";
    public static final int NONE = 0; //无状态中 初始化状态
    public static final int PLAYING = 1; //播放中
    public static final int PAUSE = 2; //暂停中
    public static final int STOP = 3; //已停止
    public static final int RELEASE = 4; //已释放

    public static final int SET_PROGRESS = 1; //设置播放录音进度
    public static final int RESET_PROGRESS = 2; //重置播放进度

    public static final int RECORD_START = 3; //录音计时开始
    public static final int RECORD_PROGRESS = 4;//录音时间记录
    public static final int RECORD_STOP = 5;//录音计时结束

    private AudioRecord audioRecord; //录音api
    private AudioTrack audioTrack; //播放录音api
    private boolean isRecording; // 标示当前是否正在录音
    private long playSeek; //标示当前播放位置
    private int playState = NONE; // 当前录音播放状态
    private Handler handler;
    private Context context;
    private String recordPcmPath; //录音原始文件 pcm
    private String recordWavPath; //录音转换文件 wav


    private RecordHelper() {
    }


    @Override
    public void startRecord() {
        //录音是否已经开启
        if (isRecording) {
            LogUtils.d(TAG, "当前正在录音中...");
            return;
        }
        //是否正在播放录音
        if (playState == PLAYING) {
            LogUtils.d(TAG, "当前正在播放录音...");
            return;
        }
        //开始计时
        handler.sendEmptyMessage(RECORD_START);

        //获取音频录制需要的最小内存
        final int minBufferSize = getMinBufferSize();
        //初始化AudioRecord
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
        );

        //创建文件夹
        File recordPcm = getRecordPcm();
        if (!recordPcm.mkdirs()) {
            LogUtils.d("Directory not created");
        }
        //覆盖文件
        if (recordPcm.exists()) {
            recordPcm.delete();
        }

        //开启录音通道
        audioRecord.startRecording();
        isRecording = true;
        //初始化一个字节数组 大小设置为上面获取的最小内存值大小用来装载录音数据
        byte[] data = new byte[minBufferSize];

        //开启一个线程读取录音数据
        new Thread(() -> {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(recordPcm);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (null != fos) {
                //开启死循环连续读取录音数据 当点击停止按钮是 标志位设为false 读取结束
                while (isRecording) {
                    int read = audioRecord.read(data, 0, minBufferSize);
                    //如果读取音频数据没有出现错误，就将数据写入到文件
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            fos.write(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                handler.sendEmptyMessage(RECORD_STOP);
                try {
                    LogUtils.i(TAG, "run: close file output stream !");
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    @Override
    public void stopRecord() {
        isRecording = false;
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void initAudioTrack() {
        if (audioTrack == null) {
            //确定音频属性
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            //确定音频格式化参数 采样率，通道数，输出编码格式
            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setSampleRate(AudioFormat.SAMPLE_RATE_UNSPECIFIED)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build();

            audioTrack = new AudioTrack(
                    audioAttributes,
                    audioFormat,
                    getMinBufferSize(),
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
            );
        }
    }

    @Override
    public void playInModeStream() {
        if (isRecording) {
            LogUtils.d(TAG, "当前正在录音中...");
            return;
        }
        if (playState == PLAYING) {
            LogUtils.d(TAG, "当前正在播放录音...");
            return;
        }
        //设置当前播放状态 '播放中'
        playState = PLAYING;
        //获取当前设备录音需要的最小内存
        int minBufferSize = getMinBufferSize();
        //初始化audioTrack
        initAudioTrack();
        //开始播放
        audioTrack.play();
        //开始读取临时文件 xx.pcm 然后播放语音
        File recordPcm = getRecordPcm();

        //开启一个线程 边读取文件数据 边解码播放动作
        new Thread(() -> {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(recordPcm);
                fis.getChannel().position(playSeek);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (null != fis) {
                //判断当前是否处于播放状态
                byte[] tempBuffer = new byte[minBufferSize];
                int readCount;
                Message msg;
                try {
                    while (fis.available() > 0 && playState == PLAYING) {
                        //根据文件指针的位置往后读取录音数据
                        readCount = fis.read(tempBuffer, 0, minBufferSize);
                        playSeek += readCount;
                        if (readCount == AudioTrack.ERROR_BAD_VALUE || readCount == AudioTrack.ERROR_INVALID_OPERATION) {
                            continue;
                        }
                        if (readCount != 0 && readCount != -1) {
                            //将读取的数据写入audioTrack 实现播放录音
                            audioTrack.write(tempBuffer, 0, readCount);
                            //计算当前读取文件的进度值
                            msg = Message.obtain();
                            msg.what = SET_PROGRESS;
                            long length = recordPcm.length();
                            double r = (double) playSeek / (double) length;
                            int progress = (int) (r * 100);
                            msg.arg1 = progress;
                            handler.sendMessage(msg);
                        }
                    }

                    if (playState == PAUSE) {
                        //结束播放时 判断当前是否已经读取到了末尾 如果是重置进度值
                        if (playSeek == recordPcm.length()) {
                            playSeek = 0;
                            msg = Message.obtain();
                            msg.what = RESET_PROGRESS;
                            handler.sendMessage(msg);
                        }
                    } else if (playState == STOP || playState == RELEASE) {
                        playSeek = 0;
                        msg = Message.obtain();
                        msg.what = RESET_PROGRESS;
                        handler.sendMessage(msg);
                    }
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void playPause() {
        playState = PAUSE;
    }

    @Override
    public void playStop() {
        playState = STOP;
    }

    @Override
    public int getMinBufferSize() {
        return AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
    }

    @Override
    public File getRecordPcm() {
        if (StringUtils.isEmpty(recordPcmPath) || !new File(recordPcmPath).exists()) {
            return new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), GlobalConfig.PCM_FILE_NAME);
        } else {
            return new File(recordPcmPath);
        }
    }


    @Override
    public File getRecordWav() {
        if (StringUtils.isEmpty(recordWavPath) && !new File(recordWavPath).exists()) {
            return new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), GlobalConfig.WAV_FILE_NAME);
        } else {
            return new File(recordWavPath);
        }
    }


    public String getDefaultRecordPcmPath() {
        return Environment.DIRECTORY_MUSIC + File.separator + GlobalConfig.PCM_FILE_NAME;
    }

    public String getDefaultRecordWavPath(){
        return Environment.DIRECTORY_MUSIC + File.separator + GlobalConfig.WAV_FILE_NAME;

    }

    public static final class RecordHelperBuilder {
        private Handler handler;
        private Context context;
        private String recordPcmPath; //录音原始文件 pcm
        private String recordWavPath; //录音转换文件 wav

        public static RecordHelperBuilder aRecordHelper() {
            return new RecordHelperBuilder();
        }

        public RecordHelperBuilder withHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public RecordHelperBuilder withContext(Context context) {
            this.context = context;
            return this;
        }

        public RecordHelperBuilder withRecordPcmPath(String recordPcmPath) {
            this.recordPcmPath = recordPcmPath;
            return this;
        }

        public RecordHelperBuilder withRecordWavPath(String recordWavPath) {
            this.recordWavPath = recordWavPath;
            return this;
        }

        public RecordHelper build() {
            RecordHelper recordHelper = new RecordHelper();
            recordHelper.context = this.context;
            recordHelper.recordWavPath = this.recordWavPath;
            recordHelper.recordPcmPath = this.recordPcmPath;
            recordHelper.handler = this.handler;
            return recordHelper;
        }
    }
}

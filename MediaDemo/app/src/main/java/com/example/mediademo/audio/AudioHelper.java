package com.example.mediademo.audio;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioHelper {

    private String srcPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/video.mp4";
    private String pcmPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/test.acc";
    private String mp4Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/test.h264";
    private MediaExtractor mediaExtractor;

    public void decode() {
        File pcmFile = new File(pcmPath);
        File mp4File = new File(mp4Path);

        try {
            if (pcmFile.exists()) {
                pcmFile.delete();
            }
            pcmFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            if (mp4File.exists()) {
                mp4File.delete();
            }
            mp4File.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //此类可分离视频文件的音轨和视频轨道
            mediaExtractor = new MediaExtractor();
            //设置目标文件
            mediaExtractor.setDataSource(srcPath);
            //打印日志 获取当前视频的轨道数 一般音频和视频轨道都有就是两个
            LogUtils.d("==========getTrackCount()===================" + mediaExtractor.getTrackCount());
            //遍历媒体轨道，包括视频和音频轨道
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                //获取媒体信息 根据参数可以判断是音频还是视频
                MediaFormat format = mediaExtractor.getTrackFormat(i);

                String mine = format.getString(MediaFormat.KEY_MIME);


                //-----------------------------获取音频轨道---------------------------------
                if (mine.startsWith("audio")) {
                    //选择此音频轨道
                    mediaExtractor.selectTrack(i);
                    //打印媒体信息
                    logAudioFormat(format);

                    try {
                        //新建一个字节缓冲区域 分配大小为100KB
                        ByteBuffer inputBuffer = ByteBuffer.allocate(100 * 1024);
                        //新建文件流 以追加形式写入文件
                        FileOutputStream fos = new FileOutputStream(pcmFile, true);
                        //从视频文件中循环读取数据 写入音频文件中
                        while (true) {
                            //读取数据放入inputBuffer中 每次最多读取100KB
                            int readSampleCount = mediaExtractor.readSampleData(inputBuffer, 0);
                            //如果数据为空 则退出循环
                            if (readSampleCount < 0) {
                                break;
                            }
                            //新建一个临时字节数组 数组大小为当前读取的字节流大小
                            byte[] buffer = new byte[readSampleCount];
                            //将改革读取的数据 写入临时字节数组
                            inputBuffer.get(buffer);
                            //将临时字节数组写入输出流中 也就是目标文件 pcmFile
                            fos.write(buffer);
                            fos.flush();
                            //清空缓冲区
                            inputBuffer.clear();
                            //获取下一帧数据
                            mediaExtractor.advance();
                        }
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //-----------------------------获取视频轨道---------------------------------
                if (mine.startsWith("video")) {
                    //选择此视频轨道
                    mediaExtractor.selectTrack(i);
                    //打印媒体信息
                    logVideoFormat(format);

                    try {
                        //新建一个字节缓冲区
                        ByteBuffer inputBuffer = ByteBuffer.allocate(100 * 1024);
                        //以追加的形式 生成一个输入流
                        FileOutputStream fos = new FileOutputStream(mp4Path, true);

                        //开始循环读取视频数据流
                        while (true) {
                            //将数据流读取到缓冲区
                            int readSampleCount = mediaExtractor.readSampleData(inputBuffer, 0);
                            //如果读取为空了 则退出循环
                            if (readSampleCount < 0) {
                                break;
                            }
                            //新建一个临时字节数组变量 用于存储缓冲区的字节数据
                            byte[] buffer = new byte[readSampleCount];
                            //将缓冲区数据写入临时字节数组中
                            inputBuffer.get(buffer);
                            //将临时变量数组写入到目标文件中
                            fos.write(buffer);
                            //清空缓冲区
                            inputBuffer.clear();
                            //获取下一帧数据
                            mediaExtractor.advance();
                        }
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }

    private void logAudioFormat(MediaFormat format) {
        LogUtils.d("====audio=====KEY_MIME=========" + format.getString(MediaFormat.KEY_MIME));
        LogUtils.d("====audio=====KEY_CHANNEL_COUNT=======" + format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) + "");
        LogUtils.d("====audio=====KEY_SAMPLE_RATE===========" + format.getInteger(MediaFormat.KEY_SAMPLE_RATE) + "");
        LogUtils.d("====audio=====KEY_DURATION===========" + format.getLong(MediaFormat.KEY_DURATION) + "");

        LogUtils.d("====audio=====getSampleFlags===========" + mediaExtractor.getSampleFlags() + "");
        LogUtils.d("====audio=====getSampleTime===========" + mediaExtractor.getSampleTime() + "");
        //  System.out.println("====audio=====getSampleSize==========="+mediaExtractor.getSampleSize()+"");api28
        LogUtils.d("====audio=====getSampleTrackIndex===========" + mediaExtractor.getSampleTrackIndex() + "");
    }

    private void logVideoFormat(MediaFormat format) {
        LogUtils.d("mine : " + format.getString(MediaFormat.KEY_MIME));
        LogUtils.d("duration : " + format.getLong(MediaFormat.KEY_DURATION));
        LogUtils.d("getSampleFlags : " + mediaExtractor.getSampleFlags());
        LogUtils.d("getSampleTime :" + mediaExtractor.getSampleTime());
        LogUtils.d("getSampleTrackIndex :" + mediaExtractor.getSampleTrackIndex());

        LogUtils.d("====video=====KEY_MIME===========" + format.getString(MediaFormat.KEY_MIME));
        LogUtils.d("====video=====KEY_DURATION===========" + format.getLong(MediaFormat.KEY_DURATION) + "");
        LogUtils.d("====video=====KEY_WIDTH===========" + format.getInteger(MediaFormat.KEY_WIDTH) + "");
        LogUtils.d("====video=====KEY_HEIGHT===========" + format.getInteger(MediaFormat.KEY_HEIGHT) + "");
        LogUtils.d("====video=====getSampleFlags===========" + mediaExtractor.getSampleFlags() + "");
        LogUtils.d("====video=====getSampleTime===========" + mediaExtractor.getSampleTime() + "");
        // System.out.println("====video=====getSampleSize==========="+mediaExtractor.getSampleSize()+"");api28
        LogUtils.d("====video=====getSampleTrackIndex===========" + mediaExtractor.getSampleTrackIndex() + "");
    }

}

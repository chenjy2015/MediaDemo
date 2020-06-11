package com.example.mediademo.audio;

import java.io.File;

public interface IRecord {

    void startRecord();
    void stopRecord();
    void playInModeStream();
    void playPause();
    void playStop();
    int getMinBufferSize();
    File getRecordPcm();
    File getRecordWav();

}

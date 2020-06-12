package com.example.mediademo.audio

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AudioInfo(var path: String, var length: Long) : MediaInfo(), Parcelable {
    lateinit var recordWavPath: String //音频WAV格式地址
}
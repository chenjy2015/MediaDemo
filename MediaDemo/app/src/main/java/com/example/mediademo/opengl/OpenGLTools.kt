package com.example.mediademo.opengl

import android.opengl.GLES20

class OpenGLTools {
    companion object {
        fun createTextureIds(count: Int): IntArray {
            val texture = IntArray(count)
            GLES20.glGenTextures(count, texture, 0) //生成纹理
            return texture
        }
    }
}
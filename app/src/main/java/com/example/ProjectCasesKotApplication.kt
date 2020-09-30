package com.example

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.coder.ffmpeg.jni.FFmpegCommand

/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/14 15:24
 * @version 1.0
 * desc $
 **/
class ProjectCasesKotApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        FFmpegCommand.setDebug(true)

    }
}
package com.example.projectcaseskot.logic.present

import com.coder.ffmpeg.call.IFFmpegCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.coder.ffmpeg.utils.FFmpegUtils
import com.example.projectcaseskot.utils.LogUtils
import com.example.projectcaseskot.view.ClipView

/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/18 11:08
 * @version 1.0
 * desc $
 **/
class ClipPresent : BasePresent<ClipView>() {
    fun concatVideo(srcFile: String, targetFile: String) {
        val startTime = System.currentTimeMillis();
        FFmpegCommand.runAsync(FFmpegUtils.concatVideo(srcFile, targetFile),
            object : IFFmpegCallBack {
                override fun onError(t: Throwable?) {
                    TODO("Not yet implemented")
                }

                override fun onProgress(progress: Int) {
                    LogUtils.d("FFmpeg", "FFmpeg_Clip:${System.currentTimeMillis() - startTime} ")
                }

                override fun onComplete() {
                    TODO("Not yet implemented")
                }

                override fun onStart() {
                    TODO("Not yet implemented")
                }

                override fun onCancel() {
                    TODO("Not yet implemented")
                }
            })
    }
}
package com.example.projectcaseskot.utils

import android.content.Context
import android.content.Intent
import com.example.projectcaseskot.constant.FFmpegTypeConstant
import com.example.projectcaseskot.ui.ClipActivity
import com.ykbjson.lib.screening.bean.DeviceInfo

/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/18 10:32
 * @version 1.0
 * desc $
 **/
object JumpUtils {
    fun jump(context: Context, type: Int) {
        when (type) {
            FFmpegTypeConstant.CLIP -> {//可以在intent后面增加apply来添加Extra
                val intent = Intent(context, ClipActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    fun projection(context: Context,deviceInfo:DeviceInfo){

    }
}
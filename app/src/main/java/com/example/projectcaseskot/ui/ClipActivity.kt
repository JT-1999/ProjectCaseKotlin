package com.example.projectcaseskot.ui

import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.view.View
import com.example.projectcaseskot.R
import com.example.projectcaseskot.constant.SourceTypeConstant.REQUEST_MEDIA_CLIP_S1
import com.example.projectcaseskot.constant.SourceTypeConstant.REQUEST_MEDIA_CLIP_S2
import com.example.projectcaseskot.logic.present.ClipPresent
import com.example.projectcaseskot.ui.base.BaseActivity
import com.example.projectcaseskot.utils.FileUtils
import com.example.projectcaseskot.utils.SnackbarUtils
import com.example.projectcaseskot.view.ClipView
import kotlinx.android.synthetic.main.activity_clip.*

/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/18 11:05
 * @version 1.0
 * desc $
 **/
class ClipActivity : BaseActivity<ClipView, ClipPresent>(), ClipView, View.OnClickListener {

    private lateinit var mMediaPath1: String

    private lateinit var mMediaPath2: String

    override fun initPresenter() = ClipPresent()

    override fun getLayoutId() = R.layout.activity_clip


    override fun initView() {

    }

    override fun initData() {

    }

    override fun initListener() {
        clipSource1.setOnClickListener(this)
        clipSource2.setOnClickListener(this)
        clipClip.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.clipSource1 -> {
                selectVideo(REQUEST_MEDIA_CLIP_S1)
            }
            R.id.clipSource2 -> {
                selectVideo(REQUEST_MEDIA_CLIP_S2)
            }
            R.id.clipClip -> SnackbarUtils.show(v, "来了！")
        }
    }

    private fun selectVideo(type: Int) {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, type)
    }

    private fun selectAudio() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, REQUEST_MEDIA_CLIP_S1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_MEDIA_CLIP_S1 -> {
                if (resultCode != RESULT_OK && data == null) {
                    return
                }
                val uri = data?.data
                var path = ""
                if (uri != null) {
                    path = if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                        FileUtils.getRealPathFromUriAboveApi19(this, uri)
                    } else {
                        uri.path!!
                    }
                }
                mMediaPath1 = path
                slipSource1Text.text = mMediaPath1
            }
            REQUEST_MEDIA_CLIP_S2 -> {
                if (resultCode != RESULT_OK && data == null) {
                    return
                }
                val uri = data?.data
                var path = ""
                if (uri != null) {
                    path = if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                        FileUtils.getRealPathFromUriAboveApi19(this, uri)
                    } else {
                        uri.path!!
                    }
                }
                mMediaPath2 = path
                slipSource1Text.text = mMediaPath2
            }
        }
    }

    


}
package com.example.projectcaseskot.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.projectcaseskot.R
import com.example.projectcaseskot.utils.SnackbarUtils
import com.ykbjson.lib.screening.bean.MediaInfo.*
import kotlinx.android.synthetic.main.activity_select_net.*

class SelectNetActivity : AppCompatActivity() {
    var urlType = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_net)

        button_confirm.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (urlType == 0) {
                    SnackbarUtils.show(editText_url, "请选择URL类型")
                    return
                }
                if (TextUtils.isEmpty(editText_url.text.toString())) {
                    SnackbarUtils.show(editText_url, "请输入正确URL路径")
                    return
                }
                val i = Intent()
                i.putExtra("Type", urlType)
                i.putExtra("URI", editText_url.text.toString().trim())
                setResult(RESULT_OK, i)
                Log.d("yjt", "$urlType ")
                finish()
            }

        })

        radioGroup_url.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.RadioButtonSelectVideo ->
                    urlType = TYPE_VIDEO

                R.id.RadioButtonSelectAudio ->
                    urlType = TYPE_AUDIO

                R.id.RadioButtonSelectPicture ->
                    urlType = TYPE_IMAGE
            }
        }
    }
}
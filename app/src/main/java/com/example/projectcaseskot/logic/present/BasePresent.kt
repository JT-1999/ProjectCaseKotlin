package com.example.projectcaseskot.logic.present

import androidx.annotation.NonNull

/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/15 14:13
 * @version 1.0
 * desc $
 **/
open  class BasePresent<T> {
    var mView: T? = null

    fun attach(@NonNull mView: T?) {
        this.mView = mView
    }

    fun dettach() {
        mView = null
    }

    fun isAttach(): Boolean {
        return mView!=null
    }
}
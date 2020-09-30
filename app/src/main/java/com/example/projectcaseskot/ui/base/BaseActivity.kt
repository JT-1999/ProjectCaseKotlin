package com.example.projectcaseskot.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projectcaseskot.R
import com.example.projectcaseskot.logic.present.BasePresent
import com.gyf.immersionbar.ktx.immersionBar


/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/15 13:58
 * @version 1.0
 * desc $
 **/
abstract class BaseActivity<V, T : BasePresent<V>> : BaseContainer, AppCompatActivity() {
    open var mTag = ""
    open var mPresenter: T? = null
    protected val _mActivity = BaseActivity::class

    abstract fun initPresenter(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        mTag = this.localClassName
        mPresenter = initPresenter()
        initImmersionBar()
        initView()
        initData()
        initListener()
        mPresenter!!.attach(this as V)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter!!.dettach()
    }

    private fun initImmersionBar() {
        immersionBar {
            statusBarColor(R.color.colorCasePrimary)
            navigationBarColor(R.color.colorCasePrimary)
        }
    }
}
package com.example.projectcaseskot.ui.base

/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/15 14:00
 * @version 1.0
 * desc $
 **/
interface BaseContainer {


    /**
     * 取得视图
     **/
    fun getLayoutId(): Int

    /**
     * 初始化View
     **/
    fun initView()

    /**
     * 初始化数据
     **/
    fun initData()

    /**
     * 初始化监听器
     **/
    fun initListener()
}
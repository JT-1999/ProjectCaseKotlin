package com.example.projectcaseskot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ProjectCasesKotApplication.Companion.context
import com.example.projectcaseskot.adapter.DeviceAdapter
import com.example.projectcaseskot.logic.present.MainPresenter
import com.example.projectcaseskot.ui.SelectNetActivity
import com.example.projectcaseskot.ui.base.BaseActivity
import com.example.projectcaseskot.utils.FileUtils
import com.example.projectcaseskot.utils.LogUtils
import com.example.projectcaseskot.utils.SnackbarUtils
import com.example.projectcaseskot.view.MainView
import com.example.simplepermission.PermissionsManager
import com.example.simplepermission.PermissionsRequestCallback
import com.ykbjson.lib.screening.DLNAManager
import com.ykbjson.lib.screening.DLNAPlayer
import com.ykbjson.lib.screening.bean.DeviceInfo
import com.ykbjson.lib.screening.bean.MediaInfo
import com.ykbjson.lib.screening.bean.MediaInfo.TYPE_UNKNOWN
import com.ykbjson.lib.screening.listener.DLNAControlCallback
import com.ykbjson.lib.screening.listener.DLNADeviceConnectListener
import com.ykbjson.lib.screening.listener.DLNARegistryListener
import com.ykbjson.lib.screening.listener.DLNAStateCallback
import kotlinx.android.synthetic.main.activity_main.*
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.meta.Service
import kotlin.concurrent.thread

open class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView,
    View.OnClickListener, DLNADeviceConnectListener {

    companion object {
        const val CODE_REQUEST_PERMISSION = 1010
        const val CODE_REQUEST_MEDIA = 1011
        const val CODE_REQUEST_URL = 1012
    }


    /**
     * 当前资源类型
     **/
    private var curItemType = TYPE_UNKNOWN

    private lateinit var mMediaPath: String

    private lateinit var mDeviceInfo: DeviceInfo
    private lateinit var mDLNARegistryListener: DLNARegistryListener
    var mDLNAPlayer = DLNAPlayer(context)
    var mDeviceList = ArrayList<DeviceInfo>()
    lateinit var mDevicesAdapter: DeviceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        swipeRefresh.setOnRefreshListener {
            refreshFFmpeg(mDevicesAdapter)
        }
        DLNAManager.setIsDebugMode(BuildConfig.DEBUG)

        //一次性请求所有权限
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(
            CODE_REQUEST_PERMISSION, this, object : PermissionsRequestCallback {
                //获取权限成功回调
                override fun onGranted(requestCode: Int, permission: String?) {
                    LogUtils.d(mTag, "DLNAManager ,onGranted")
                    val hasPermission = PackageManager.PERMISSION_GRANTED ==
                            (checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    and checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO))
                    if (hasPermission) {
                        DLNAManager.getInstance()
                            .init(this@MainActivity, object : DLNAStateCallback {
                                override fun onConnected() {
                                    LogUtils.d(mTag, "DLNAManager ,onConnected")
                                    initDlna()
                                    mDevicesAdapter =
                                        DeviceAdapter(this@MainActivity, mDeviceList, mDLNAPlayer)
                                    DeviceRecyclerView.adapter = mDevicesAdapter

                                }

                                override fun onDisconnected() {
                                    LogUtils.d(mTag, "holy shit ")
                                }
                            })
                    }
                }

                override fun onDenied(requestCode: Int, permission: String?) {
                    LogUtils.d(mTag, "DLNAManager ,onDenied")
                }

                override fun onDeniedForever(requestCode: Int, permission: String?) {
                    LogUtils.d(mTag, "DLNAManager ,onDeniedForever")
                }

                override fun onFailure(requestCode: Int, deniedPermissions: Array<out String>?) {
                    LogUtils.d(mTag, "DLNAManager ,onFailure")
                }

                override fun onSuccess(requestCode: Int) {
                    LogUtils.d(mTag, "DLNAManager ,onSuccess")
                }
            })
        mDevicesAdapter = DeviceAdapter(this, mDeviceList, mDLNAPlayer)
        setSupportActionBar(MainToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.item)
        }
        navView.setCheckedItem(R.id.navHistory)
        navView.setNavigationItemSelectedListener {
            drawerLayout.closeDrawers()
            true
        }

        val layoutManager = GridLayoutManager(this, 2)
        DeviceRecyclerView.layoutManager = layoutManager

    }

    override fun getLayoutId() = R.layout.activity_main

    override fun initView() {
        swipeRefresh.setColorSchemeResources(R.color.colorCasePrimary)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onConnect(deviceInfo: DeviceInfo, errorCode: Int) {
        if (errorCode == DLNADeviceConnectListener.CONNECT_INFO_CONNECT_SUCCESS) {
            mDeviceInfo = deviceInfo
            SnackbarUtils.show(DeviceRecyclerView, "连接设备成功")
        }
    }

    override fun onDisconnect(deviceInfo: DeviceInfo?, type: Int, errorCode: Int) {
        LogUtils.d(mTag, "连接设备失败")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_video -> selectVideo()

            R.id.select_audio -> selectAudio()

            R.id.select_picture -> selectImage()

            R.id.select_phone -> screeningPhone()

            R.id.select_net -> selectNet()

            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

    private fun selectNet() {
        if (!this::mDeviceInfo.isInitialized) {
            SnackbarUtils.show(DeviceRecyclerView, "请先连接设备！!")
            return
        }
        val i = Intent(this, SelectNetActivity::class.java)
        startActivityForResult(i, CODE_REQUEST_URL)
    }

    private fun selectVideo() {
        if (!this::mDeviceInfo.isInitialized) {
            SnackbarUtils.show(DeviceRecyclerView, "请先连接设备！!")
            return
        }
        curItemType = MediaInfo.TYPE_VIDEO
        val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, CODE_REQUEST_MEDIA)
    }

    private fun selectAudio() {
        if (!this::mDeviceInfo.isInitialized) {
            SnackbarUtils.show(DeviceRecyclerView, "请先连接设备！!")
            return
        }
        curItemType = MediaInfo.TYPE_AUDIO
        val i = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, CODE_REQUEST_MEDIA)
    }

    private fun selectImage() {
        if (!this::mDeviceInfo.isInitialized) {
            SnackbarUtils.show(DeviceRecyclerView, "请先连接设备！!")
            return
        }
        curItemType = MediaInfo.TYPE_IMAGE
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, CODE_REQUEST_MEDIA)
    }

    private fun screeningPhone() {
        if (!this::mDeviceInfo.isInitialized) {
            SnackbarUtils.show(DeviceRecyclerView, "请先连接设备！!")
            return
        }
        curItemType = MediaInfo.TYPE_MIRROR
        mDLNAPlayer.connect(mDeviceInfo)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_REQUEST_MEDIA) {
            if (resultCode != RESULT_OK && data == null) {
                return
            }
            val uri = data!!.data
            var path = ""
            if (uri != null) {

                path = if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                    FileUtils.getRealPathFromUriAboveApi19(this, uri)
                } else {
                    uri.path.toString()
                }
            }
            mMediaPath = path
            LogUtils.d(mTag, path)
            mDLNAPlayer.connect(mDeviceInfo)
        }
        if (requestCode == CODE_REQUEST_URL) {
            if (resultCode != RESULT_OK && data == null) {
                return
            }
            val url = data!!.getStringExtra("URI")
            curItemType = data.getIntExtra("Type", TYPE_UNKNOWN)
            Log.d(mTag, "$curItemType")
            mMediaPath = url?.toString().toString()
            mDLNAPlayer.connect(mDeviceInfo)
        }
    }


    override fun initData() {

    }

    private fun refreshFFmpeg(adapter: DeviceAdapter) {
        thread {
            DLNAManager.getInstance().startBrowser()
            runOnUiThread {
                adapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
                SnackbarUtils.show(DeviceRecyclerView, "刷新设备列表")
            }
        }
    }

    open fun initDlna() {
        mDLNAPlayer.setConnectListener(this)
        mDLNARegistryListener = object : DLNARegistryListener() {
            override fun onDeviceChanged(deviceInfoList: List<DeviceInfo>) {
                mDeviceList.clear()
                mDeviceList.addAll(deviceInfoList)
                mDevicesAdapter.notifyDataSetChanged()
            }
        }
        DLNAManager.getInstance().registerListener(mDLNARegistryListener)
    }

    override fun initListener() {
        fab_right.setOnClickListener(this)
        fab_left.setOnClickListener(this)
        control_play.setOnClickListener(this)
        control_pause.setOnClickListener(this)
        control_forward.setOnClickListener(this)
        control_mute.setOnClickListener(this)
        navView.setCheckedItem(R.id.navHistory)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navHistory -> SnackbarUtils.show(findViewById(R.id.fab_right), "你点击了“历 史”")
                R.id.navNotification -> SnackbarUtils.show(
                    findViewById(R.id.fab_right),
                    "你点击了“历 史”"
                )
                R.id.navSetting -> SnackbarUtils.show(findViewById(R.id.fab_right), "你点击了“历 史”")
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    override fun initPresenter(): MainPresenter {
        return MainPresenter()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_right -> startPlay()

            R.id.control_play -> mDLNAPlayer.play(object : DLNAControlCallback {
                override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
                    Toast.makeText(this@MainActivity, "开始播放", Toast.LENGTH_SHORT).show()
                }

                override fun onReceived(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    vararg extra: Any?
                ) {
                }

                override fun onFailure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    errorCode: Int,
                    errorMsg: String?
                ) {
                    Toast.makeText(this@MainActivity, "继续播放失败", Toast.LENGTH_SHORT).show()
                }

            })

            R.id.control_pause -> mDLNAPlayer.pause(object : DLNAControlCallback {
                override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
                    Toast.makeText(this@MainActivity, "暂停成功", Toast.LENGTH_SHORT).show()
                }

                override fun onReceived(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    vararg extra: Any?
                ) {

                }

                override fun onFailure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    errorCode: Int,
                    errorMsg: String?
                ) {
                    Toast.makeText(this@MainActivity, "暂停失败", Toast.LENGTH_SHORT).show()
                }

            })

            R.id.control_mute -> mDLNAPlayer.mute(true, object : DLNAControlCallback {
                override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
                    Toast.makeText(this@MainActivity, "静音成功", Toast.LENGTH_SHORT).show()
                }

                override fun onReceived(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    vararg extra: Any?
                ) {
                    Toast.makeText(this@MainActivity, "why?", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    errorCode: Int,
                    errorMsg: String?
                ) {
                    Toast.makeText(this@MainActivity, "静音失败", Toast.LENGTH_SHORT).show()
                }
            })

            R.id.control_forward -> mDLNAPlayer.seekTo("5", object : DLNAControlCallback {
                override fun onSuccess(invocation: ActionInvocation<out Service<*, *>>?) {
                    Toast.makeText(this@MainActivity, "快进5秒", Toast.LENGTH_SHORT).show()
                }

                override fun onReceived(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    vararg extra: Any?
                ) {
                }

                override fun onFailure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    errorCode: Int,
                    errorMsg: String?
                ) {
                    Toast.makeText(this@MainActivity, "快进失败,$String", Toast.LENGTH_SHORT).show()
                }

            })

            R.id.fab_left -> {
                if (swipeRefresh.visibility == View.GONE) {
                    swipeRefresh.visibility = View.VISIBLE
                    control_LinearLayout.visibility = View.GONE
                } else {
                    swipeRefresh.visibility = View.GONE
                    control_LinearLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 开始播放
     */
    private fun startPlay() {
        val sourceUrl: String = mMediaPath
        val mediaInfo = MediaInfo()
        if (!TextUtils.isEmpty(sourceUrl)) {
            mediaInfo.mediaId = Base64.encodeToString(sourceUrl.toByteArray(), Base64.NO_WRAP)
            mediaInfo.uri = sourceUrl
        }
        mediaInfo.mediaType = curItemType
        mDLNAPlayer.setDataSource(mediaInfo)

        mDLNAPlayer.start(object : DLNAControlCallback {
            override fun onSuccess(invocation: ActionInvocation<*>?) {
                Toast.makeText(this@MainActivity, "投屏成功", Toast.LENGTH_SHORT).show()
                swipeRefresh.visibility = View.GONE
                control_LinearLayout.visibility = View.VISIBLE

            }

            override fun onReceived(invocation: ActionInvocation<*>?, vararg extra: Any?) {}
            override fun onFailure(
                invocation: ActionInvocation<*>?,
                errorCode: Int,
                errorMsg: String?
            ) {
                Toast.makeText(this@MainActivity, "投屏失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        mDLNAPlayer.destroy()
        DLNAManager.getInstance().unregisterListener(mDLNARegistryListener)
        DLNAManager.getInstance().destroy()
        super.onDestroy()
    }
}
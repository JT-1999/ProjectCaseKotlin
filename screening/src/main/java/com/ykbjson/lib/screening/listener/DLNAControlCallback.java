package com.ykbjson.lib.screening.listener;


import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import org.fourthline.cling.model.action.ActionInvocation;

/**
 * Description：DLNA执行命令回调接口
 * <BR/>
 * Creator：yankebin
 * <BR/>
 * CreatedAt：2019-07-10
 */
public interface DLNAControlCallback {
    int ERROR_CODE_NO_ERROR = 0;

    int ERROR_CODE_RE_PLAY = 1;

    int ERROR_CODE_RE_PAUSE = 2;

    int ERROR_CODE_RE_STOP = 3;

    int ERROR_CODE_DLNA_ERROR = 4;

    int ERROR_CODE_SERVICE_ERROR = 5;

    int ERROR_CODE_NOT_READY = 6;

    int ERROR_CODE_BIND_SCREEN_RECORDER_SERVICE_ERROR = 7;

    //播放暂停结束静音开闭音量调整等只会回调onSuccess和onFailure方法
    void onSuccess(@Nullable ActionInvocation invocation);

    //获取播放进这种需要获取结果的方法会在onReceived方法里返回结果
    void onReceived(@Nullable ActionInvocation invocation, @Nullable Object... extra);

    void onFailure(@Nullable ActionInvocation invocation,
                   @IntRange(from = ERROR_CODE_NO_ERROR, to = ERROR_CODE_BIND_SCREEN_RECORDER_SERVICE_ERROR) int errorCode,
                   @Nullable String errorMsg);
}

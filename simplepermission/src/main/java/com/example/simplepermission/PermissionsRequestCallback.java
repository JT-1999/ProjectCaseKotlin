package com.example.simplepermission;

/**
 * projectName ProjectCasesKot
 *
 * @author JT
 * @version 1.0
 * desc 权限申请结果回调
 * @since 2020/9/27 10:32
 **/
public interface PermissionsRequestCallback {
    /**
     *将权限敏感代码在此处执行
     */
    void onGranted(int requestCode, String permission);

    /**
     *当权限被拒绝时调用此方法
     * @param permission the permission that was denied.
     */
    void onDenied(int requestCode, String permission);

    /**
     *权限被永远拒绝时 调用此方法
     * @param permission the permission that was denied.
     */
    void onDeniedForever(int requestCode, String permission);

    /**
     * This method is called when all permissions has been check complete
     * but some permissions denied.
     *
     * @param deniedPermissions those denied permissions
     */
    void onFailure(int requestCode, String[] deniedPermissions);

    /**
     * This method is called when all permissions has been check complete
     * and all permissions granted.
     */
    void onSuccess(int requestCode);
}

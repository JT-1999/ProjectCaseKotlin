package com.example.simplepermission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * projectName ProjectCasesKot
 *
 * @author JT
 * @version 1.0
 * desc $
 * @since 2020/9/27 11:08
 **/
public class PermissionsManager {
    private final String TAG = getClass().getName();
    private final Set<String> mPendingRequests = new HashSet<>(1);
    private final Set<String> mPermissions = new HashSet<>(1);
    private final List<PermissionsResultAction> mPendingActions = new ArrayList<>(1);

    private boolean enableLog;
    private static volatile PermissionsManager mInstance = null;

    public static PermissionsManager getInstance() {
        if (null == mInstance) {
            synchronized (PermissionsManager.class) {
                if (mInstance == null) {
                    mInstance = new PermissionsManager();
                }
            }
        }
        return mInstance;
    }

    private PermissionsManager() {
        initializePermissionsMap();
    }

    /**
     * This method uses reflection to read all the permissions in the Manifest class.
     * This is necessary because some permissions do not exist on older versions of Android,
     * since they do not exist, they will be denied when you check whether you have permission
     * which is problematic since a new permission is often added where there was no previous
     * permission required. We initialize a Set of available permissions and check the set
     * when checking if we have permission since we want to know when we are denied a permission
     * because it doesn't exist yet.
     * <p>
     * 此方法使用反射读取清单类中的所有权限。这是必要的，因为有些权限在旧版本的Android上不存在
     * ，因为它们不存在，所以当您检查您是否拥有权限时，这些权限将被拒绝
     * ，这是因为在以前不需要权限的情况下通常会添加新的权限。我们初始化一组可用的权限
     * ，并在检查是否有权限时检查该组，因为我们想知道何时因为权限不存在而被拒绝。
     */
    private synchronized void initializePermissionsMap() {
        Field[] fields = Manifest.permission.class.getFields();
        for (Field field : fields) {
            String name = null;
            try {
                name = (String) field.get("");
            } catch (IllegalAccessException e) {
                if (enableLog) {
                    Log.d(TAG, "Could not access field", e);
                }
            }
            mPermissions.add(name);
        }
    }

    /**
     * This method retrieves all the permissions declared in the application's manifest.
     * It returns a non null array of permisions that can be declared.
     * <p>
     * 此方法检索应用程序清单中声明的所有权限。它返回可以声明的权限的非空数组。
     *
     * @param activity the Activity necessary to check what permissions we have.
     * @return a non null array of permissions that are declared in the application manifest.
     */
    @NonNull
    public synchronized String[] getManifestPermissions(@NonNull final Activity activity) {
        PackageInfo packageInfo = null;
        List<String> list = new ArrayList<>(1);
        try {
            if (enableLog) {
                Log.d(TAG, activity.getPackageName());
            }
            packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            if (enableLog) {
                Log.d(TAG, "A problem occurred when retrieving permissions", e);
            }
        }
        if (packageInfo != null) {
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions != null) {
                for (String perm : permissions) {
                    if (enableLog) {
                        Log.d(TAG, "Manifest contained permission: " + perm);
                    }
                    list.add(perm);

                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * This method adds the {@link PermissionsResultAction} to the current list
     * of pending actions that will be completed when the permissions are
     * received. The list of permissions passed to this method are registered
     * in the PermissionsResultAction object so that it will be notified of changes
     * made to these permissions.
     * <p>
     * 此方法将{@link PermissionsResultAction}添加到当前等待操作列表中
     * ，这些操作将在收到权限时完成。传递给此方法的权限列表在PermissionsResultAction对象中注册
     * ，以便它将被通知对这些权限的更改。
     *
     * @param permissions the required permissions for the action to be executed.
     * @param action      the action to add to the current list of pending actions.
     */
    private synchronized void addPendingAction(@NonNull String[] permissions,
                                               @Nullable PermissionsResultAction action) {
        if (action == null) {
            return;
        }
        action.registerPermissions(permissions);
        mPendingActions.add(action);
    }

    /**
     * This method removes a pending action from the list of pending actions.
     * It is used for cases where the permission has already been granted, so
     * you immediately wish to remove the pending action from the queue and
     * execute the action.
     *
     * @param action the action to remove
     */
    private synchronized void removePendingAction(@Nullable PermissionsResultAction action) {
        for (Iterator<PermissionsResultAction> iterator = mPendingActions.iterator();
             iterator.hasNext(); ) {
            PermissionsResultAction weakRef = iterator.next();
            if (weakRef == action || weakRef == null) {
                iterator.remove();
            }
        }
    }

    /**
     * This static method can be used to check whether or not you have a specific permission.
     * It is basically a less verbose method of using {@link ActivityCompat#checkSelfPermission(Context, String)}
     * and will simply return a boolean whether or not you have the permission. If you pass
     * in a null Context object, it will return false as otherwise it cannot check the permission.
     * However, the Activity parameter is nullable so that you can pass in a reference that you
     * are not always sure will be valid or not (e.g. getActivity() from Fragment).
     *
     * @param context    the Context necessary to check the permission
     * @param permission the permission to check
     * @return true if you have been granted the permission, false otherwise
     */
    public synchronized boolean hasPermission(@Nullable Context context, @NonNull String permission) {
        return context != null && (PermissionChecker.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED || !mPermissions.contains(permission));
    }

    /**
     * This static method can be used to check whether or not you have several specific permissions.
     * It is simpler than checking using {@link ActivityCompat#checkSelfPermission(Context, String)}
     * for each permission and will simply return a boolean whether or not you have all the permissions.
     * If you pass in a null Context object, it will return false as otherwise it cannot check the
     * permission. However, the Activity parameter is nullable so that you can pass in a reference
     * that you are not always sure will be valid or not (e.g. getActivity() from Fragment).
     *
     * @param context     the Context necessary to check the permission
     * @param permissions the permissions to check
     * @return true if you have been granted all the permissions, false otherwise
     */
    public synchronized boolean hasAllPermissions(@Nullable Context context, @NonNull String[] permissions) {
        if (context == null) {
            return false;
        }
        boolean hasAllPermissions = true;
        for (String perm : permissions) {
            hasAllPermissions &= hasPermission(context, perm);
        }
        return hasAllPermissions;
    }

    /**
     * This static method can be used to check whether or not you have several specific permissions.
     * It is simpler than checking using {@link ActivityCompat#checkSelfPermission(Context, String)}
     * for each permission and will simply return a boolean whether or not you have all the permissions.
     * If you pass in a null Context object, it will return false as otherwise it cannot check the
     * permission. However, the Activity parameter is nullable so that you can pass in a reference
     * that you are not always sure will be valid or not (e.g. getActivity() from Fragment).
     *
     * @param context the Context necessary to check the permission
     * @return true if you have been granted all the permissions, false otherwise
     */
    public synchronized boolean hasAllPermissions(@Nullable Activity context) {
        if (context == null) {
            return false;
        }
        String[] perms = getManifestPermissions(context);
        return hasAllPermissions(context, perms);
    }

    /**
     * This method will request all the permissions declared in your application manifest
     * for the specified {@link PermissionsResultAction}. The purpose of this method is to enable
     * all permissions to be requested at one shot. The PermissionsResultAction is used to notify
     * you of the user allowing or denying each permission. The Activity and PermissionsResultAction
     * parameters are both annotated Nullable, but this method will not work if the Activity
     * is null. It is only annotated Nullable as a courtesy to prevent crashes in the case
     * that you call this from a Fragment where {@link Fragment#getActivity()} could yield
     * null. Additionally, you will not receive any notification of permissions being granted
     * if you provide a null PermissionsResultAction.
     * <p>
     * 此方法将请求指定的{@link PermissionsResultAction}在应用程序清单中声明的所有权限。
     * 此方法的目的是使所有权限可以一次性被请求。
     * PermissionsResultAction用于通知您用户允许或拒绝每个权限。
     * Activity和PermissionsResultAction参数都可注释为空，但是如果活动为空，此方法将无法工作。
     * 它被标注为Nullable只是为了避免从一个{@link Fragment#getActivity()}
     * 可能产生null值的片段中调用时崩溃。此外，您将不会收到任何授予权限的通知
     * *如果你提供了一个null PermissionsResultAction。
     *
     * @param requestCode the permissions requestCode
     * @param activity    the Activity necessary to request and check permissions.
     * @param callback    the PermissionsRequestCallback used to notify you of permissions being accepted.
     */
    public synchronized void requestAllManifestPermissionsIfNecessary(int requestCode, @Nullable Activity activity,
                                                                      @Nullable PermissionsRequestCallback callback) {
        if (activity == null) {
            return;
        }
        String[] perms = getManifestPermissions(activity);
        requestPermissionsIfNecessaryForResult(requestCode, activity, perms, callback);
    }

    /**
     * This method should be used to execute a {@link PermissionsResultAction} for the array
     * of permissions passed to this method. This method will request the permissions if
     * they need to be requested (i.e. we don't have permission yet) and will add the
     * PermissionsResultAction to the queue to be notified of permissions being granted or
     * denied. In the case of pre-Android Marshmallow, permissions will be granted immediately.
     * The Activity variable is nullable, but if it is null, the method will fail to execute.
     * This is only nullable as a courtesy for Fragments where getActivity() may yeild null
     * if the Fragment is not currently added to its parent Activity.
     * <p>
     * 这个方法应该用来执行一个{@link PermissionsResultAction}，用于传递给这个方法的权限数组。
     * 如果需要请求权限(也就是说，我们还没有权限)，该方法将请求权限，
     * 并将PermissionsResultAction添加到队列中，以便通知授予或拒绝权限。
     * 如果是android前版本的Marshmallow，权限将立即被授予。
     * Activity变量是可空的，但是如果它是空的，该方法将无法执行。
     * 如果片段当前没有被添加到它的父活动中，那么getActivity()可能会显示为空。
     *
     * @param requestCode the permissions requestCode
     * @param activity    the activity necessary to request the permissions.
     * @param permissions the list of permissions to request for the {@link PermissionsResultAction}.
     * @param callback    the PermissionsRequestCallback to notify when the permissions are granted or denied.
     */
    public synchronized void requestPermissionsIfNecessaryForResult(int requestCode, @Nullable Activity activity,
                                                                    @NonNull String[] permissions,
                                                                    @Nullable PermissionsRequestCallback callback) {
        if (activity == null) {
            return;
        }
        final PermissionsResultAction action = new PermissionsResultAction(requestCode, callback);
        addPendingAction(permissions, action);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            doPermissionWorkBeforeAndroidM(activity, permissions, action);
        } else {
            List<String> permList = getPermissionsListToRequest(activity, permissions, action);
            if (permList.isEmpty()) {
                //if there is no permission to request, there is no reason to keep the action in the list
                removePendingAction(action);
            } else {
                String[] permsToRequest = permList.toArray(new String[permList.size()]);
                mPendingRequests.addAll(permList);
                ActivityCompat.requestPermissions(activity, permsToRequest, 1);
            }
        }
    }

    /**
     * This method should be used to execute a {@link PermissionsResultAction} for the array
     * of permissions passed to this method. This method will request the permissions if
     * they need to be requested (i.e. we don't have permission yet) and will add the
     * PermissionsResultAction to the queue to be notified of permissions being granted or
     * denied. In the case of pre-Android Marshmallow, permissions will be granted immediately.
     * The Fragment variable is used, but if {@link Fragment#getActivity()} returns null, this method
     * will fail to work as the activity reference is necessary to check for permissions.
     *
     * @param requestCode the permissions requestCode
     * @param fragment    the fragment necessary to request the permissions.
     * @param permissions the list of permissions to request for the {@link PermissionsResultAction}.
     * @param callback    the PermissionsRequestCallback to notify when the permissions are granted or denied.
     */
    public synchronized void requestPermissionsIfNecessaryForResult(int requestCode, @NonNull Fragment fragment,
                                                                    @NonNull String[] permissions,
                                                                    @Nullable PermissionsRequestCallback callback) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        final PermissionsResultAction action = new PermissionsResultAction(requestCode, callback);
        addPendingAction(permissions, action);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            doPermissionWorkBeforeAndroidM(activity, permissions, action);
        } else {
            List<String> permList = getPermissionsListToRequest(activity, permissions, action);
            if (permList.isEmpty()) {
                //if there is no permission to request, there is no reason to keep the action int the list
                removePendingAction(action);
            } else {
                String[] permsToRequest = permList.toArray(new String[permList.size()]);
                mPendingRequests.addAll(permList);
                fragment.requestPermissions(permsToRequest, 1);
            }
        }
    }


    /**
     * This method should be used to execute a {@link PermissionsResultAction} for the array
     * of permissions passed to this method. This method will request the permissions if
     * they need to be requested (i.e. we don't have permission yet) and will add the
     * PermissionsResultAction to the queue to be notified of permissions being granted or
     * denied. In the case of pre-Android Marshmallow, permissions will be granted immediately.
     * The Fragment variable is used, but if {@link android.app.Fragment#getActivity()} returns null, this method
     * will fail to work as the activity reference is necessary to check for permissions.
     *
     * @param requestCode the permissions requestCode
     * @param fragment    the android.app.fragment necessary to request the permissions.
     * @param permissions the list of permissions to request for the {@link PermissionsResultAction}.
     * @param callback    the PermissionsRequestCallback to notify when the permissions are granted or denied.
     */
    public synchronized void requestPermissionsIfNecessaryForResult(int requestCode, @NonNull android.app.Fragment fragment,
                                                                    @NonNull String[] permissions,
                                                                    @Nullable PermissionsRequestCallback callback) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        final PermissionsResultAction action = new PermissionsResultAction(requestCode, callback);
        addPendingAction(permissions, action);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            doPermissionWorkBeforeAndroidM(activity, permissions, action);
        } else {
            List<String> permList = getPermissionsListToRequest(activity, permissions, action);
            if (permList.isEmpty()) {
                //if there is no permission to request, there is no reason to keep the action int the list
                removePendingAction(action);
            } else {
                String[] permsToRequest = permList.toArray(new String[permList.size()]);
                mPendingRequests.addAll(permList);
                fragment.requestPermissions(permsToRequest, 1);
            }
        }
    }

    /**
     * This method notifies the PermissionsManager that the permissions have change. If you are making
     * the permissions requests using an Activity, then this method should be called from the
     * Activity callback onRequestPermissionsResult() with the variables passed to that method. If
     * you are passing a Fragment to make the permissions request, then you should call this in
     * the {@link Fragment#onRequestPermissionsResult(int, String[], int[])} method.
     * It will notify all the pending PermissionsResultAction objects currently
     * in the queue, and will remove the permissions request from the list of pending requests.
     *
     * 此方法通知PermissionsManager权限已更改。如果使用活动发出权限请求，则应从
     * 活动回调onRequestPermissionsResult()，以及传递给该方法的变量。
     * 它将通知当前所有挂起的PermissionsResultAction对象
     * ，并将从挂起的请求列表中删除权限请求。
     *
     * @param permissions the permissions that have changed.
     * @param results     the values for each permission.
     */
    public synchronized void notifyPermissionsChange(@NonNull String[] permissions, @NonNull int[] results) {
        int size = permissions.length;
        if (results.length < size) {
            size = results.length;
        }
        Iterator<PermissionsResultAction> iterator = mPendingActions.iterator();
        while (iterator.hasNext()) {
            PermissionsResultAction action = iterator.next();
            for (int n = 0; n < size; n++) {
                if (action == null || action.onResult(permissions[n], results[n])) {
                    iterator.remove();
                    break;
                }
            }
        }
        for (int n = 0; n < size; n++) {
            mPendingRequests.remove(permissions[n]);
        }
    }

    /**
     * When request permissions on devices before Android M (Android 6.0, API Level 23)
     * Do the granted or denied work directly according to the permission status
     *
     * 请求Android M之前的设备权限时 根据
     * 权限请求结果 直接完成被批准或被拒绝的工作
     * @param activity    the activity to check permissions
     * @param permissions the permissions names
     * @param action      the callback work object, containing what we what to do after
     *                    permission check
     */
    private void doPermissionWorkBeforeAndroidM(@NonNull Activity activity,
                                                @NonNull String[] permissions,
                                                @Nullable PermissionsResultAction action) {
        for (String perm : permissions) {
            if (action != null) {
                if (!mPermissions.contains(perm)) {
                    action.onResult(perm, Permissions.NOT_FOUND);
                } else if (ActivityCompat.checkSelfPermission(activity, perm)
                        != PackageManager.PERMISSION_GRANTED) {
                    action.onResult(perm, Permissions.DENIED);
                } else {
                    action.onResult(perm, Permissions.GRANTED);
                }
            }
        }
    }

    /**
     * Filter the permissions list:
     * If a permission is not granted, add it to the result list
     * if a permission is granted, do the granted work, do not add it to the result list
     *
     * 过滤权限列表:
     * 如果未授予权限，请将其添加到结果列表中。
     * 如果已授予权限，请执行已授予的工作，不要将其添加到结果列表中
     *
     * @param activity    the activity to check permissions
     * @param permissions all the permissions names
     * @param action      the callback work object, containing what we what to do after
     *                    permission check
     * @return a list of permissions names that are not granted yet
     */
    @NonNull
    private List<String> getPermissionsListToRequest(@NonNull Activity activity,
                                                     @NonNull String[] permissions,
                                                     @Nullable PermissionsResultAction action) {
        List<String> permList = new ArrayList<>(permissions.length);
        for (String perm : permissions) {
            if (!mPermissions.contains(perm)) {
                if (action != null) {
                    action.onResult(perm, Permissions.NOT_FOUND);
                }
            } else if (PermissionChecker.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                    if (action != null) {
                        action.onResult(perm, Permissions.USER_DENIED_FOREVER);
                    }
                } else if (!mPendingRequests.contains(perm)) {
                    permList.add(perm);
                }
            } else {
                if (action != null) {
                    action.onResult(perm, Permissions.GRANTED);
                }
            }
        }
        return permList;
    }

    /**
     * Set Log status
     *
     * @param enableLog Log status value
     */
    public void setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
    }

    /**
     * return Log status
     *
     * @return Log status
     */
    public boolean isEnableLog() {
        return enableLog;
    }
}

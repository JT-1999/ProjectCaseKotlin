package com.example.simplepermission;

/**
 * projectName ProjectCasesKot
 *
 * @author JT
 * @version 1.0
 * desc Enum class to handle the different states
 * of permissions since the PackageManager only
 * has a granted and denied state.
 * @since 2020/9/27 11:06
 **/
public enum Permissions {
    GRANTED,
    DENIED,
    NOT_FOUND,
    USER_DENIED_FOREVER
}

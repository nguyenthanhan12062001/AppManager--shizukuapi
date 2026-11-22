package com.shizukumanager.app.model;

// Phan loai loi he thong khi thuc thi qua Shizuku hoac PackageManager
public enum ErrorType {
    NONE,
    SHIZUKU_NOT_INSTALLED,
    SHIZUKU_NOT_RUNNING,
    PERMISSION_DENIED,
    BINDER_LOST,
    PACKAGE_NOT_FOUND,
    SUSPEND_UNSUPPORTED,
    INSUFFICIENT_PRIVILEGE,
    SELF_OPERATION_BLOCKED,
    STATE_NOT_CHANGED,
    UNKNOWN
}

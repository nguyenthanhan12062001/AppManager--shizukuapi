package com.shizukumanager.app.util;

import android.content.Context;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Lop bao ve cac goi he thong quan trong de tranh loi thiet bi hoac tu vo hieu hoa
public class PackageSafetyGuard {
    private static final Set<String> CRITICAL_PACKAGES = new HashSet<>(Arrays.asList(
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.phone",
            "com.android.providers.settings",
            "com.android.shell",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "moe.shizuku.privileged.api"
    ));

    private final String selfPackageName;

    public PackageSafetyGuard(Context context) {
        this.selfPackageName = context.getApplicationContext().getPackageName();
    }

    public boolean isSelfPackage(String packageName) {
        return selfPackageName.equals(packageName);
    }

    public boolean isCriticalSystemPackage(String packageName) {
        if (packageName == null) {
            return false;
        }
        return CRITICAL_PACKAGES.contains(packageName) || isSelfPackage(packageName);
    }

    // Khong cho phep Suspend goi he thong Android cot loi hoac chinh Shizuku
    public boolean isSuspendSupportedPolicy(String packageName, boolean isSystemCore) {
        if (packageName == null) {
            return false;
        }
        if (CRITICAL_PACKAGES.contains(packageName) || isSelfPackage(packageName)) {
            return false;
        }
        return !"android".equals(packageName) && !isSystemCore;
    }
}

package com.shizukumanager.app.pkg;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.pm.PackageInfoCompat;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.util.PackageSafetyGuard;
import java.util.ArrayList;
import java.util.List;

// Lop truy van truc tiep PackageManager cua Android de lay trang thai thuc te
public class SystemPackageScanner {
    private final PackageManager packageManager;
    private final PackageSafetyGuard safetyGuard;

    public SystemPackageScanner(Context context, PackageSafetyGuard safetyGuard) {
        this.packageManager = context.getApplicationContext().getPackageManager();
        this.safetyGuard = safetyGuard;
    }

    // Quet tat ca ung dung da cai dat cho nguoi dung hien tai (loai tru goi da go cai dat)
    public List<AppInfoModel> scanInstalledApplications() {
        int flags = PackageManager.MATCH_DISABLED_COMPONENTS
                | PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS;

        List<PackageInfo> packages;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packages = packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags));
        } else {
            packages = packageManager.getInstalledPackages(flags);
        }

        List<AppInfoModel> result = new ArrayList<>(packages.size());
        for (PackageInfo pkgInfo : packages) {
            AppInfoModel model = mapPackageInfo(pkgInfo);
            if (model != null) {
                result.add(model);
            }
        }
        return result;
    }

    // Truy van lai mot goi cu the de xac minh trang thai sau khi thuc thi lenh
    public AppInfoModel querySinglePackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return null;
        }
        int flags = PackageManager.MATCH_DISABLED_COMPONENTS
                | PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS;
        try {
            PackageInfo pkgInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags));
            } else {
                pkgInfo = packageManager.getPackageInfo(packageName, flags);
            }
            return mapPackageInfo(pkgInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private AppInfoModel mapPackageInfo(PackageInfo pkgInfo) {
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            return null;
        }
        ApplicationInfo appInfo = pkgInfo.applicationInfo;

        // Dam bao chi lay goi thuc su da cai dat cho user hien tai, khong nham voi goi uninstalled
        boolean installedForUser = (appInfo.flags & ApplicationInfo.FLAG_INSTALLED) != 0;
        if (!installedForUser) {
            return null;
        }

        String packageName = pkgInfo.packageName;
        CharSequence labelSeq = packageManager.getApplicationLabel(appInfo);
        String appName = (labelSeq != null && labelSeq.length() > 0) ? labelSeq.toString() : packageName;
        String versionName = pkgInfo.versionName != null ? pkgInfo.versionName : "";
        long versionCode = PackageInfoCompat.getLongVersionCode(pkgInfo);

        boolean systemApp = (appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;

        int enabledSetting;
        try {
            enabledSetting = packageManager.getApplicationEnabledSetting(packageName);
        } catch (IllegalArgumentException e) {
            return null;
        }

        boolean isDisabled = !appInfo.enabled
                || enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                || enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED;

        boolean isSuspended = (appInfo.flags & ApplicationInfo.FLAG_SUSPENDED) != 0;
        if (!isSuspended && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                isSuspended = packageManager.isPackageSuspended(packageName);
            } catch (Throwable ignored) {
                // Co the nem SecurityException neu khong co quyen truy van dac biet, da fallback bang FLAG_SUSPENDED
            }
        }

        return new AppInfoModel(
                packageName,
                appName,
                versionName,
                versionCode,
                appInfo.uid,
                appInfo.sourceDir != null ? appInfo.sourceDir : "",
                pkgInfo.firstInstallTime,
                pkgInfo.lastUpdateTime,
                systemApp,
                !isDisabled,
                enabledSetting,
                isSuspended,
                safetyGuard.isCriticalSystemPackage(packageName)
        );
    }
}

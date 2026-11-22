package com.shizukumanager.app.model;

// Mo hinh du lieu ung dung duoc truy van truc tiep tu he thong Android PackageManager
public class AppInfoModel {
    private final String packageName;
    private final String appName;
    private final String versionName;
    private final long versionCode;
    private final int uid;
    private final String apkPath;
    private final long installTimeMillis;
    private final long lastUpdateTimeMillis;
    private final boolean systemApp;
    private final boolean enabled;
    private final int enabledSetting;
    private final boolean suspended;
    private final boolean criticalSystemPackage;

    public AppInfoModel(
            String packageName,
            String appName,
            String versionName,
            long versionCode,
            int uid,
            String apkPath,
            long installTimeMillis,
            long lastUpdateTimeMillis,
            boolean systemApp,
            boolean enabled,
            int enabledSetting,
            boolean suspended,
            boolean criticalSystemPackage
    ) {
        this.packageName = packageName;
        this.appName = appName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.uid = uid;
        this.apkPath = apkPath;
        this.installTimeMillis = installTimeMillis;
        this.lastUpdateTimeMillis = lastUpdateTimeMillis;
        this.systemApp = systemApp;
        this.enabled = enabled;
        this.enabledSetting = enabledSetting;
        this.suspended = suspended;
        this.criticalSystemPackage = criticalSystemPackage;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public String getVersionName() {
        return versionName;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public int getUid() {
        return uid;
    }

    public String getApkPath() {
        return apkPath;
    }

    public long getInstallTimeMillis() {
        return installTimeMillis;
    }

    public long getLastUpdateTimeMillis() {
        return lastUpdateTimeMillis;
    }

    public boolean isSystemApp() {
        return systemApp;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getEnabledSetting() {
        return enabledSetting;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public boolean isCriticalSystemPackage() {
        return criticalSystemPackage;
    }

    // Tinh trang thai tong hop de hien thi ro rang cho nguoi dung
    public AppState getAppState() {
        if (!enabled && suspended) {
            return AppState.DISABLED_AND_SUSPENDED;
        }
        if (!enabled) {
            return AppState.DISABLED;
        }
        if (suspended) {
            return AppState.SUSPENDED;
        }
        return AppState.ENABLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppInfoModel)) return false;
        AppInfoModel that = (AppInfoModel) o;
        return versionCode == that.versionCode
                && uid == that.uid
                && lastUpdateTimeMillis == that.lastUpdateTimeMillis
                && systemApp == that.systemApp
                && enabled == that.enabled
                && enabledSetting == that.enabledSetting
                && suspended == that.suspended
                && packageName.equals(that.packageName)
                && appName.equals(that.appName);
    }

    @Override
    public int hashCode() {
        return packageName.hashCode();
    }
}

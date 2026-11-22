package com.shizukumanager.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Bang luu metadata ung dung cuc bo (chi la bo dem phu, khong thay the PackageManager)
@Entity(tableName = "app_metadata")
public class AppMetadataEntity {
    @PrimaryKey
    @NonNull
    public String packageName;

    @NonNull
    public String appName;

    public String versionName;
    public long versionCode;
    public boolean systemApp;
    public String lastObservedState;
    public long lastSyncedAt;

    public AppMetadataEntity(
            @NonNull String packageName,
            @NonNull String appName,
            String versionName,
            long versionCode,
            boolean systemApp,
            String lastObservedState,
            long lastSyncedAt
    ) {
        this.packageName = packageName;
        this.appName = appName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.systemApp = systemApp;
        this.lastObservedState = lastObservedState;
        this.lastSyncedAt = lastSyncedAt;
    }
}

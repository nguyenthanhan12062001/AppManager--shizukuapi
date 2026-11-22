package com.shizukumanager.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

// Bang luu thong tin cau hinh quan ly trang thai ung dung (App Profile)
@Entity(tableName = "app_profiles")
public class AppProfileEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    @NonNull
    public String description;

    public long createdAt;
    public long updatedAt;
    public long lastAppliedAt;

    public AppProfileEntity() {
        this.name = "";
        this.description = "";
    }

    @Ignore
    public AppProfileEntity(@NonNull String name, @NonNull String description, long createdAt, long updatedAt) {
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastAppliedAt = 0L;
    }
}

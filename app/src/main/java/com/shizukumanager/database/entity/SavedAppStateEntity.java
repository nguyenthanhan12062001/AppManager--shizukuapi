package com.shizukumanager.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Bang luu trang thai truoc khi ap dung Profile de ho tro khoi phuc chinh xac
@Entity(
        tableName = "saved_app_states",
        foreignKeys = @ForeignKey(
                entity = AppProfileEntity.class,
                parentColumns = "id",
                childColumns = "profileId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("profileId"), @Index(value = {"profileId", "packageName"}, unique = true)}
)
public class SavedAppStateEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long profileId;

    @NonNull
    public String packageName;

    @NonNull
    public String appName;

    public boolean wasEnabled;
    public boolean wasSuspended;
    public long savedAt;

    public SavedAppStateEntity(
            long profileId,
            @NonNull String packageName,
            @NonNull String appName,
            boolean wasEnabled,
            boolean wasSuspended,
            long savedAt
    ) {
        this.profileId = profileId;
        this.packageName = packageName;
        this.appName = appName;
        this.wasEnabled = wasEnabled;
        this.wasSuspended = wasSuspended;
        this.savedAt = savedAt;
    }
}

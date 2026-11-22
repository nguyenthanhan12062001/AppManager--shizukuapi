package com.shizukumanager.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Bang lien ket tung ung dung va hanh dong muc tieu thuoc mot App Profile
@Entity(
        tableName = "profile_apps",
        foreignKeys = @ForeignKey(
                entity = AppProfileEntity.class,
                parentColumns = "id",
                childColumns = "profileId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("profileId"), @Index(value = {"profileId", "packageName"}, unique = true)}
)
public class ProfileAppEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long profileId;

    @NonNull
    public String packageName;

    @NonNull
    public String appName;

    @NonNull
    public String targetAction;

    public ProfileAppEntity(
            long profileId,
            @NonNull String packageName,
            @NonNull String appName,
            @NonNull String targetAction
    ) {
        this.profileId = profileId;
        this.packageName = packageName;
        this.appName = appName;
        this.targetAction = targetAction;
    }
}

package com.shizukumanager.app.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Bang luu lich su cac thao tac quan ly goi ung dung
@Entity(tableName = "operation_history")
public class OperationHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String packageName;

    @NonNull
    public String appName;

    @NonNull
    public String operationType;

    @NonNull
    public String previousState;

    @NonNull
    public String newState;

    public boolean success;

    @NonNull
    public String errorMessage;

    public long timestampMillis;

    public OperationHistoryEntity(
            @NonNull String packageName,
            @NonNull String appName,
            @NonNull String operationType,
            @NonNull String previousState,
            @NonNull String newState,
            boolean success,
            @NonNull String errorMessage,
            long timestampMillis
    ) {
        this.packageName = packageName;
        this.appName = appName;
        this.operationType = operationType;
        this.previousState = previousState;
        this.newState = newState;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestampMillis = timestampMillis;
    }
}

package com.shizukumanager.app.model;

// Ket qua chi tiet sau khi thuc hien thao tac tren mot ung dung
public class OperationResult {
    private final String packageName;
    private final String appName;
    private final OperationType operationType;
    private final AppState previousState;
    private final AppState newState;
    private final boolean success;
    private final ErrorType errorType;
    private final String rawErrorDetail;
    private final long timestampMillis;

    public OperationResult(
            String packageName,
            String appName,
            OperationType operationType,
            AppState previousState,
            AppState newState,
            boolean success,
            ErrorType errorType,
            String rawErrorDetail,
            long timestampMillis
    ) {
        this.packageName = packageName;
        this.appName = appName;
        this.operationType = operationType;
        this.previousState = previousState;
        this.newState = newState;
        this.success = success;
        this.errorType = errorType == null ? ErrorType.NONE : errorType;
        this.rawErrorDetail = rawErrorDetail == null ? "" : rawErrorDetail;
        this.timestampMillis = timestampMillis;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public AppState getPreviousState() {
        return previousState;
    }

    public AppState getNewState() {
        return newState;
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getRawErrorDetail() {
        return rawErrorDetail;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }
}

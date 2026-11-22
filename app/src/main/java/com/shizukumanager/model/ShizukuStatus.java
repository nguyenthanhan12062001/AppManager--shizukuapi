package com.shizukumanager.app.model;

// Thong tin chi tiet trang thai ket noi va phien ban Shizuku
public class ShizukuStatus {
    private final ShizukuConnectionState state;
    private final int apiVersion;
    private final int uid;
    private final boolean userServiceBound;

    public ShizukuStatus(
            ShizukuConnectionState state,
            int apiVersion,
            int uid,
            boolean userServiceBound
    ) {
        this.state = state;
        this.apiVersion = apiVersion;
        this.uid = uid;
        this.userServiceBound = userServiceBound;
    }

    public ShizukuConnectionState getState() {
        return state;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public int getUid() {
        return uid;
    }

    public boolean isUserServiceBound() {
        return userServiceBound;
    }

    public boolean isReady() {
        return state == ShizukuConnectionState.CONNECTED;
    }
}

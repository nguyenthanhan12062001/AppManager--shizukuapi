package com.shizukumanager.app.model;

// Thong ke thuc te cua he thong cho man hinh Dashboard
public class AppDashboardStats {
    private final int totalApps;
    private final int enabledApps;
    private final int disabledApps;
    private final int suspendedApps;

    public AppDashboardStats(int totalApps, int enabledApps, int disabledApps, int suspendedApps) {
        this.totalApps = totalApps;
        this.enabledApps = enabledApps;
        this.disabledApps = disabledApps;
        this.suspendedApps = suspendedApps;
    }

    public int getTotalApps() {
        return totalApps;
    }

    public int getEnabledApps() {
        return enabledApps;
    }

    public int getDisabledApps() {
        return disabledApps;
    }

    public int getSuspendedApps() {
        return suspendedApps;
    }
}

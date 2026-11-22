package com.shizukumanager.app.pkg;

import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.AppState;
import com.shizukumanager.app.model.ErrorType;
import com.shizukumanager.app.model.OperationResult;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.model.ShizukuConnectionState;
import com.shizukumanager.app.model.ShizukuStatus;
import com.shizukumanager.app.shizuku.CommandExecutionResult;
import com.shizukumanager.app.shizuku.ShizukuConnectionManager;
import com.shizukumanager.app.util.PackageSafetyGuard;
import java.util.Locale;

// Thuc thi cac thao tac Enable, Disable, Suspend, Unsuspend, Freeze, Unfreeze, Force Stop va xac minh lai trang thai
public class PackageOperationsManager {
    private final ShizukuConnectionManager shizukuManager;
    private final SystemPackageScanner packageScanner;
    private final PackageSafetyGuard safetyGuard;

    public PackageOperationsManager(
            ShizukuConnectionManager shizukuManager,
            SystemPackageScanner packageScanner,
            PackageSafetyGuard safetyGuard
    ) {
        this.shizukuManager = shizukuManager;
        this.packageScanner = packageScanner;
        this.safetyGuard = safetyGuard;
    }

    public OperationResult executeOperation(String packageName, String fallbackAppName, OperationType operationType) {
        long now = System.currentTimeMillis();
        AppInfoModel before = packageScanner.querySinglePackage(packageName);
        String resolvedName = before != null ? before.getAppName() : (fallbackAppName != null ? fallbackAppName : packageName);
        AppState previousState = before != null ? before.getAppState() : AppState.ENABLED;

        if (before == null) {
            return new OperationResult(
                    packageName,
                    resolvedName,
                    operationType,
                    previousState,
                    previousState,
                    false,
                    ErrorType.PACKAGE_NOT_FOUND,
                    "Package not installed for current user",
                    now
            );
        }

        if (safetyGuard.isSelfPackage(packageName) && operationType != OperationType.ENABLE && operationType != OperationType.UNSUSPEND && operationType != OperationType.UNFREEZE) {
            return new OperationResult(
                    packageName,
                    resolvedName,
                    operationType,
                    previousState,
                    previousState,
                    false,
                    ErrorType.SELF_OPERATION_BLOCKED,
                    "Self modification blocked",
                    now
            );
        }

        ShizukuStatus status = shizukuManager.refreshStatus();
        if (status.getState() == ShizukuConnectionState.NOT_INSTALLED) {
            return failWith(packageName, resolvedName, operationType, previousState, ErrorType.SHIZUKU_NOT_INSTALLED, "", now);
        }
        if (status.getState() == ShizukuConnectionState.SERVICE_NOT_RUNNING) {
            return failWith(packageName, resolvedName, operationType, previousState, ErrorType.SHIZUKU_NOT_RUNNING, "", now);
        }
        if (status.getState() == ShizukuConnectionState.PERMISSION_REQUIRED) {
            return failWith(packageName, resolvedName, operationType, previousState, ErrorType.PERMISSION_DENIED, "", now);
        }

        String[] cmd = buildCommandArgs(packageName, operationType);
        if (cmd == null) {
            return failWith(packageName, resolvedName, operationType, previousState, ErrorType.UNKNOWN, "Unsupported operation", now);
        }

        CommandExecutionResult exec = shizukuManager.executeShellCommand(cmd);
        if (exec.isBinderLost()) {
            return failWith(packageName, resolvedName, operationType, previousState, ErrorType.BINDER_LOST, exec.getStderr(), now);
        }

        // Cho he thong cap nhat PackageManager va truy van lai trang thai thuc te
        AppInfoModel after = packageScanner.querySinglePackage(packageName);
        if (after == null) {
            return failWith(packageName, resolvedName, operationType, previousState, ErrorType.PACKAGE_NOT_FOUND, exec.getStderr(), now);
        }

        AppState newState = after.getAppState();
        String combinedOutput = (exec.getStdout() + " " + exec.getStderr()).trim();

        if (!exec.isExitZero() || hasCommandFailureText(combinedOutput)) {
            ErrorType mappedError = classifyCommandError(operationType, combinedOutput);
            return new OperationResult(
                    packageName,
                    resolvedName,
                    operationType,
                    previousState,
                    newState,
                    false,
                    mappedError,
                    combinedOutput,
                    now
            );
        }

        boolean verified = verifyExpectedState(operationType, after);
        if (!verified) {
            return new OperationResult(
                    packageName,
                    resolvedName,
                    operationType,
                    previousState,
                    newState,
                    false,
                    ErrorType.STATE_NOT_CHANGED,
                    combinedOutput,
                    now
            );
        }

        return new OperationResult(
                packageName,
                resolvedName,
                operationType,
                previousState,
                newState,
                true,
                ErrorType.NONE,
                "",
                now
        );
    }

    private String[] buildCommandArgs(String packageName, OperationType type) {
        switch (type) {
            case ENABLE:
                return new String[]{"pm", "enable", packageName};
            case DISABLE:
                return new String[]{"pm", "disable-user", "--user", "0", packageName};
            case SUSPEND:
            case FREEZE:
                return new String[]{"pm", "suspend", packageName};
            case UNSUSPEND:
            case UNFREEZE:
                return new String[]{"pm", "unsuspend", packageName};
            case FORCE_STOP:
                return new String[]{"am", "force-stop", packageName};
            default:
                return null;
        }
    }

    private boolean verifyExpectedState(OperationType type, AppInfoModel after) {
        switch (type) {
            case ENABLE:
                return after.isEnabled();
            case DISABLE:
                return !after.isEnabled();
            case SUSPEND:
            case FREEZE:
                return after.isSuspended();
            case UNSUSPEND:
            case UNFREEZE:
                return !after.isSuspended();
            case FORCE_STOP:
                return true;
            default:
                return false;
        }
    }

    private boolean hasCommandFailureText(String output) {
        if (output == null || output.isEmpty()) {
            return false;
        }
        String lower = output.toLowerCase(Locale.ROOT);
        return lower.contains("exception")
                || lower.contains("error")
                || lower.contains("failed")
                || lower.contains("cannot suspend")
                || lower.contains("not allowed")
                || lower.contains("permission denial")
                || lower.contains("unknown package");
    }

    private ErrorType classifyCommandError(OperationType type, String output) {
        String lower = output == null ? "" : output.toLowerCase(Locale.ROOT);
        if (lower.contains("unknown package") || lower.contains("not found")) {
            return ErrorType.PACKAGE_NOT_FOUND;
        }
        if (lower.contains("securityexception") || lower.contains("permission denial") || lower.contains("not allowed")) {
            if (type == OperationType.SUSPEND || type == OperationType.FREEZE) {
                return ErrorType.SUSPEND_UNSUPPORTED;
            }
            return ErrorType.INSUFFICIENT_PRIVILEGE;
        }
        if (lower.contains("suspend") || lower.contains("cannot suspend") || lower.contains("unsupported")) {
            return ErrorType.SUSPEND_UNSUPPORTED;
        }
        return ErrorType.UNKNOWN;
    }

    private OperationResult failWith(
            String packageName,
            String appName,
            OperationType op,
            AppState prevState,
            ErrorType errorType,
            String detail,
            long now
    ) {
        return new OperationResult(
                packageName,
                appName,
                op,
                prevState,
                prevState,
                false,
                errorType,
                detail,
                now
        );
    }
}

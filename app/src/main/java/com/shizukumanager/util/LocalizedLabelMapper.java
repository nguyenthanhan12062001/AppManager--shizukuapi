package com.shizukumanager.app.util;

import android.content.Context;
import com.shizukumanager.app.R;
import com.shizukumanager.app.model.AppState;
import com.shizukumanager.app.model.ErrorType;
import com.shizukumanager.app.model.OperationType;

// Anh xa ma loi, trang thai va loai thao tac sang chuoi tai nguyen da ngon ngu
public final class LocalizedLabelMapper {
    private LocalizedLabelMapper() {
    }

    public static String getErrorText(Context context, ErrorType errorType, String rawDetail) {
        if (errorType == null) {
            return context.getString(R.string.err_unknown, rawDetail == null ? "" : rawDetail);
        }
        switch (errorType) {
            case SHIZUKU_NOT_INSTALLED:
                return context.getString(R.string.err_shizuku_not_installed);
            case SHIZUKU_NOT_RUNNING:
                return context.getString(R.string.err_shizuku_not_running);
            case PERMISSION_DENIED:
                return context.getString(R.string.err_shizuku_permission);
            case BINDER_LOST:
                return context.getString(R.string.err_binder_lost);
            case PACKAGE_NOT_FOUND:
                return context.getString(R.string.err_package_not_found);
            case SUSPEND_UNSUPPORTED:
                return context.getString(R.string.err_suspend_unsupported);
            case INSUFFICIENT_PRIVILEGE:
                return context.getString(R.string.err_insufficient_privilege);
            case SELF_OPERATION_BLOCKED:
                return context.getString(R.string.err_self_operation);
            case STATE_NOT_CHANGED:
                return context.getString(R.string.err_state_not_changed);
            case NONE:
                return "";
            case UNKNOWN:
            default:
                return context.getString(R.string.err_unknown, rawDetail == null ? "" : rawDetail);
        }
    }

    public static String getOperationLabel(Context context, OperationType type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case ENABLE:
                return context.getString(R.string.op_enable);
            case DISABLE:
                return context.getString(R.string.op_disable);
            case SUSPEND:
                return context.getString(R.string.op_suspend);
            case UNSUSPEND:
                return context.getString(R.string.op_unsuspend);
            case FREEZE:
                return context.getString(R.string.op_freeze);
            case UNFREEZE:
                return context.getString(R.string.op_unfreeze);
            case FORCE_STOP:
                return context.getString(R.string.op_force_stop);
            case UNINSTALL:
                return context.getString(R.string.op_uninstall);
            default:
                return type.name();
        }
    }

    public static String getStateLabel(Context context, AppState state) {
        if (state == null) {
            return context.getString(R.string.value_unknown);
        }
        switch (state) {
            case ENABLED:
                return context.getString(R.string.state_enabled);
            case DISABLED:
                return context.getString(R.string.state_disabled);
            case SUSPENDED:
                return context.getString(R.string.state_suspended);
            case DISABLED_AND_SUSPENDED:
                return context.getString(R.string.state_disabled_suspended);
            default:
                return state.name();
        }
    }
}

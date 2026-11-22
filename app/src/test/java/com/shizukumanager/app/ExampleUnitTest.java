package com.shizukumanager.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.AppState;
import com.shizukumanager.app.model.BulkOperationSummary;
import com.shizukumanager.app.model.ErrorType;
import com.shizukumanager.app.model.OperationResult;
import com.shizukumanager.app.model.OperationType;
import java.util.Arrays;
import org.junit.Test;

// Kiem thu don vi cac mo hinh trang thai ung dung va tong hop ket qua hang loat
public class ExampleUnitTest {

    @Test
    public void appState_distinguishesDisabledAndSuspended() {
        AppInfoModel enabledApp = new AppInfoModel("com.a", "A", "1.0", 1, 1000, "/a.apk", 1, 1, false, true, 0, false, false);
        AppInfoModel disabledApp = new AppInfoModel("com.b", "B", "1.0", 1, 1001, "/b.apk", 1, 1, false, false, 3, false, false);
        AppInfoModel suspendedApp = new AppInfoModel("com.c", "C", "1.0", 1, 1002, "/c.apk", 1, 1, false, true, 0, true, false);

        assertEquals(AppState.ENABLED, enabledApp.getAppState());
        assertEquals(AppState.DISABLED, disabledApp.getAppState());
        assertEquals(AppState.SUSPENDED, suspendedApp.getAppState());
    }

    @Test
    public void bulkSummary_reportsPartialSuccessAccurately() {
        OperationResult ok = new OperationResult("com.a", "A", OperationType.DISABLE, AppState.ENABLED, AppState.DISABLED, true, ErrorType.NONE, "", 1L);
        OperationResult fail = new OperationResult("com.b", "B", OperationType.DISABLE, AppState.ENABLED, AppState.ENABLED, false, ErrorType.INSUFFICIENT_PRIVILEGE, "denied", 2L);

        BulkOperationSummary summary = new BulkOperationSummary(OperationType.DISABLE, Arrays.asList(ok, fail));
        assertFalse(summary.isAllSuccess());
        assertTrue(summary.isPartialSuccess());
        assertEquals(1, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
    }
}

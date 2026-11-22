package com.shizukumanager.app.model;

import java.util.Collections;
import java.util.List;

// Tong hop ket qua thuc thi hang loat hoac ap dung cau hinh
public class BulkOperationSummary {
    private final OperationType operationType;
    private final List<OperationResult> results;
    private final int successCount;
    private final int failureCount;

    public BulkOperationSummary(OperationType operationType, List<OperationResult> results) {
        this.operationType = operationType;
        this.results = results == null ? Collections.emptyList() : Collections.unmodifiableList(results);
        int ok = 0;
        int fail = 0;
        for (OperationResult r : this.results) {
            if (r.isSuccess()) {
                ok++;
            } else {
                fail++;
            }
        }
        this.successCount = ok;
        this.failureCount = fail;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public List<OperationResult> getResults() {
        return results;
    }

    public int getTotalCount() {
        return results.size();
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public boolean isAllSuccess() {
        return failureCount == 0 && successCount > 0;
    }

    public boolean isPartialSuccess() {
        return successCount > 0 && failureCount > 0;
    }
}

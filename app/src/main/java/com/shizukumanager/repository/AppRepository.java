package com.shizukumanager.app.repository;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.shizukumanager.app.database.dao.AppMetadataDao;
import com.shizukumanager.app.database.dao.OperationHistoryDao;
import com.shizukumanager.app.database.entity.AppMetadataEntity;
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import com.shizukumanager.app.model.AppDashboardStats;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.BulkOperationSummary;
import com.shizukumanager.app.model.OperationResult;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.pkg.PackageOperationsManager;
import com.shizukumanager.app.pkg.SystemPackageScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

// Repository quan ly trang thai ung dung thuc te tu PackageManager va dong bo metadata Room
public class AppRepository {

    public interface SingleOperationCallback {
        void onComplete(OperationResult result);
    }

    public interface SinglePackageCallback {
        void onLoaded(AppInfoModel appInfo);
    }

    public interface BulkProgressCallback {
        void onProgress(int completed, int total, String currentAppName);
    }

    public interface BulkCompleteCallback {
        void onComplete(BulkOperationSummary summary);
    }

    private final SystemPackageScanner packageScanner;
    private final PackageOperationsManager operationsManager;
    private final AppMetadataDao metadataDao;
    private final OperationHistoryDao historyDao;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<AppInfoModel>> appsLiveData = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<AppDashboardStats> statsLiveData = new MutableLiveData<>(new AppDashboardStats(0, 0, 0, 0));
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public AppRepository(
            SystemPackageScanner packageScanner,
            PackageOperationsManager operationsManager,
            AppMetadataDao metadataDao,
            OperationHistoryDao historyDao,
            ExecutorService executor
    ) {
        this.packageScanner = packageScanner;
        this.operationsManager = operationsManager;
        this.metadataDao = metadataDao;
        this.historyDao = historyDao;
        this.executor = executor;
    }

    public LiveData<List<AppInfoModel>> observeAllApps() {
        return appsLiveData;
    }

    public LiveData<AppDashboardStats> observeStats() {
        return statsLiveData;
    }

    public LiveData<Boolean> observeLoading() {
        return loadingLiveData;
    }

    // Quet lai toan bo ung dung tu PackageManager va cap nhat bo dem Room
    public void refreshPackages() {
        loadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                List<AppInfoModel> scanned = packageScanner.scanInstalledApplications();
                updateAppsAndStats(scanned);
                syncRoomMetadataCache(scanned);
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    public void querySinglePackage(String packageName, SinglePackageCallback callback) {
        executor.execute(() -> {
            AppInfoModel model = packageScanner.querySinglePackage(packageName);
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onLoaded(model);
                }
            });
        });
    }

    public void executeSingleOperation(
            String packageName,
            String appName,
            OperationType operationType,
            SingleOperationCallback callback
    ) {
        executor.execute(() -> {
            OperationResult result = operationsManager.executeOperation(packageName, appName, operationType);
            recordHistory(result);
            List<AppInfoModel> refreshed = packageScanner.scanInstalledApplications();
            updateAppsAndStats(refreshed);
            syncRoomMetadataCache(refreshed);
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onComplete(result);
                }
            });
        });
    }

    public void executeBulkOperation(
            List<AppInfoModel> targets,
            OperationType operationType,
            BulkProgressCallback progressCallback,
            BulkCompleteCallback completeCallback
    ) {
        executor.execute(() -> {
            List<OperationResult> results = new ArrayList<>();
            int total = targets == null ? 0 : targets.size();
            for (int i = 0; i < total; i++) {
                AppInfoModel app = targets.get(i);
                final int step = i + 1;
                mainHandler.post(() -> {
                    if (progressCallback != null) {
                        progressCallback.onProgress(step, total, app.getAppName());
                    }
                });
                OperationResult res = operationsManager.executeOperation(
                        app.getPackageName(),
                        app.getAppName(),
                        operationType
                );
                results.add(res);
                recordHistory(res);
            }
            List<AppInfoModel> refreshed = packageScanner.scanInstalledApplications();
            updateAppsAndStats(refreshed);
            syncRoomMetadataCache(refreshed);
            BulkOperationSummary summary = new BulkOperationSummary(operationType, results);
            mainHandler.post(() -> {
                if (completeCallback != null) {
                    completeCallback.onComplete(summary);
                }
            });
        });
    }

    public void recordExternalHistory(OperationResult result) {
        executor.execute(() -> recordHistory(result));
    }

    private void recordHistory(OperationResult result) {
        OperationHistoryEntity entity = new OperationHistoryEntity(
                result.getPackageName(),
                result.getAppName(),
                result.getOperationType().name(),
                result.getPreviousState().name(),
                result.getNewState().name(),
                result.isSuccess(),
                result.isSuccess() ? "" : (result.getErrorType().name() + (result.getRawErrorDetail().isEmpty() ? "" : ": " + result.getRawErrorDetail())),
                result.getTimestampMillis()
        );
        historyDao.insert(entity);
    }

    private void updateAppsAndStats(List<AppInfoModel> apps) {
        int enabledCount = 0;
        int disabledCount = 0;
        int suspendedCount = 0;
        for (AppInfoModel app : apps) {
            if (app.isEnabled()) {
                enabledCount++;
            } else {
                disabledCount++;
            }
            if (app.isSuspended()) {
                suspendedCount++;
            }
        }
        appsLiveData.postValue(apps);
        statsLiveData.postValue(new AppDashboardStats(apps.size(), enabledCount, disabledCount, suspendedCount));
    }

    private void syncRoomMetadataCache(List<AppInfoModel> apps) {
        long now = System.currentTimeMillis();
        List<AppMetadataEntity> entities = new ArrayList<>(apps.size());
        List<String> activePackages = new ArrayList<>(apps.size());
        for (AppInfoModel app : apps) {
            activePackages.add(app.getPackageName());
            entities.add(new AppMetadataEntity(
                    app.getPackageName(),
                    app.getAppName(),
                    app.getVersionName(),
                    app.getVersionCode(),
                    app.isSystemApp(),
                    app.getAppState().name(),
                    now
            ));
        }
        metadataDao.upsertAll(entities);
        if (!activePackages.isEmpty()) {
            metadataDao.pruneRemovedPackages(activePackages);
        }
    }
}

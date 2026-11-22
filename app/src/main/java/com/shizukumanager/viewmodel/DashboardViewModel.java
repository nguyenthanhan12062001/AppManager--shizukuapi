package com.shizukumanager.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import com.shizukumanager.app.model.AppDashboardStats;
import com.shizukumanager.app.model.ShizukuStatus;
import com.shizukumanager.app.repository.AppRepository;
import com.shizukumanager.app.repository.HistoryRepository;
import com.shizukumanager.app.shizuku.ShizukuConnectionManager;
import java.util.List;

// ViewModel cho man hinh Tong quan (Dashboard)
public class DashboardViewModel extends ViewModel {
    private final AppRepository appRepository;
    private final HistoryRepository historyRepository;
    private final ShizukuConnectionManager shizukuManager;

    public DashboardViewModel(
            AppRepository appRepository,
            HistoryRepository historyRepository,
            ShizukuConnectionManager shizukuManager
    ) {
        this.appRepository = appRepository;
        this.historyRepository = historyRepository;
        this.shizukuManager = shizukuManager;
    }

    public LiveData<AppDashboardStats> observeStats() {
        return appRepository.observeStats();
    }

    public LiveData<Boolean> observeLoading() {
        return appRepository.observeLoading();
    }

    public LiveData<ShizukuStatus> observeShizukuStatus() {
        return shizukuManager.observeStatus();
    }

    public LiveData<List<OperationHistoryEntity>> observeRecentChanges() {
        return historyRepository.observeRecentHistory(6);
    }

    public void refreshAll() {
        shizukuManager.refreshStatus();
        shizukuManager.tryBindUserServiceIfReady();
        appRepository.refreshPackages();
    }

    public void checkShizukuConnection() {
        shizukuManager.refreshStatus();
        shizukuManager.tryBindUserServiceIfReady();
    }

    public void requestShizukuPermission() {
        shizukuManager.requestPermission();
    }
}

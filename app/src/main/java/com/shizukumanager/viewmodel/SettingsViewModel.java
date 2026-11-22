package com.shizukumanager.app.viewmodel;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.shizukumanager.app.model.ShizukuStatus;
import com.shizukumanager.app.repository.HistoryRepository;
import com.shizukumanager.app.repository.ProfileRepository;
import com.shizukumanager.app.shizuku.ShizukuConnectionManager;
import com.shizukumanager.app.util.AppPreferencesManager;
import java.util.concurrent.ExecutorService;

// ViewModel cho man hinh Cai dat (Shizuku, Giao dien, Hanh vi, Du lieu JSON)
public class SettingsViewModel extends ViewModel {

    public interface ExportReadyCallback {
        void onJsonReady(String json);
    }

    private final ShizukuConnectionManager shizukuManager;
    private final AppPreferencesManager preferencesManager;
    private final ProfileRepository profileRepository;
    private final HistoryRepository historyRepository;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public SettingsViewModel(
            ShizukuConnectionManager shizukuManager,
            AppPreferencesManager preferencesManager,
            ProfileRepository profileRepository,
            HistoryRepository historyRepository,
            ExecutorService executor
    ) {
        this.shizukuManager = shizukuManager;
        this.preferencesManager = preferencesManager;
        this.profileRepository = profileRepository;
        this.historyRepository = historyRepository;
        this.executor = executor;
    }

    public LiveData<ShizukuStatus> observeShizukuStatus() {
        return shizukuManager.observeStatus();
    }

    public void checkShizukuConnection() {
        shizukuManager.refreshStatus();
        shizukuManager.tryBindUserServiceIfReady();
    }

    public void requestShizukuPermission() {
        shizukuManager.requestPermission();
    }

    public AppPreferencesManager getPreferencesManager() {
        return preferencesManager;
    }

    public void exportProfilesJson(ExportReadyCallback callback) {
        executor.execute(() -> {
            String json = profileRepository.exportProfilesToJsonSync();
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onJsonReady(json);
                }
            });
        });
    }

    public void importProfilesJson(String json, ProfileRepository.JsonResultCallback callback) {
        profileRepository.importProfilesFromJson(json, callback);
    }

    public void clearHistory() {
        historyRepository.clearAllHistory();
    }
}

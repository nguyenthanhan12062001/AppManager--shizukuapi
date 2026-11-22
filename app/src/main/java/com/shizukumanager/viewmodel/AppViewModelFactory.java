package com.shizukumanager.app.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.shizukumanager.app.repository.AppRepository;
import com.shizukumanager.app.repository.HistoryRepository;
import com.shizukumanager.app.repository.ProfileRepository;
import com.shizukumanager.app.shizuku.ShizukuConnectionManager;
import com.shizukumanager.app.util.AppPreferencesManager;
import java.util.concurrent.ExecutorService;

// Factory tao ViewModel theo mo hinh Manual Dependency Injection
public class AppViewModelFactory implements ViewModelProvider.Factory {
    private final AppRepository appRepository;
    private final ProfileRepository profileRepository;
    private final HistoryRepository historyRepository;
    private final ShizukuConnectionManager shizukuManager;
    private final AppPreferencesManager preferencesManager;
    private final ExecutorService executor;

    public AppViewModelFactory(
            AppRepository appRepository,
            ProfileRepository profileRepository,
            HistoryRepository historyRepository,
            ShizukuConnectionManager shizukuManager,
            AppPreferencesManager preferencesManager,
            ExecutorService executor
    ) {
        this.appRepository = appRepository;
        this.profileRepository = profileRepository;
        this.historyRepository = historyRepository;
        this.shizukuManager = shizukuManager;
        this.preferencesManager = preferencesManager;
        this.executor = executor;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(appRepository, historyRepository, shizukuManager);
        }
        if (modelClass.isAssignableFrom(AppListViewModel.class)) {
            return (T) new AppListViewModel(appRepository, preferencesManager);
        }
        if (modelClass.isAssignableFrom(AppDetailsViewModel.class)) {
            return (T) new AppDetailsViewModel(appRepository, shizukuManager);
        }
        if (modelClass.isAssignableFrom(ProfilesViewModel.class)) {
            return (T) new ProfilesViewModel(profileRepository, appRepository);
        }
        if (modelClass.isAssignableFrom(HistoryViewModel.class)) {
            return (T) new HistoryViewModel(historyRepository);
        }
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(shizukuManager, preferencesManager, profileRepository, historyRepository, executor);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}

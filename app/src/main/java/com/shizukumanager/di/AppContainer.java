package com.shizukumanager.app.di;

import android.content.Context;
import com.shizukumanager.app.database.AppDatabase;
import com.shizukumanager.app.pkg.PackageOperationsManager;
import com.shizukumanager.app.pkg.SystemPackageScanner;
import com.shizukumanager.app.repository.AppRepository;
import com.shizukumanager.app.repository.HistoryRepository;
import com.shizukumanager.app.repository.ProfileRepository;
import com.shizukumanager.app.shizuku.ShizukuConnectionManager;
import com.shizukumanager.app.util.AppIconLoader;
import com.shizukumanager.app.util.AppPreferencesManager;
import com.shizukumanager.app.util.PackageSafetyGuard;
import com.shizukumanager.app.viewmodel.AppViewModelFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Manual Dependency Injection Container quan ly cac thanh phan dung chung toan ung dung
public class AppContainer {
    private final ExecutorService ioExecutor;
    private final ExecutorService iconExecutor;
    private final AppPreferencesManager preferencesManager;
    private final PackageSafetyGuard safetyGuard;
    private final ShizukuConnectionManager shizukuConnectionManager;
    private final SystemPackageScanner systemPackageScanner;
    private final PackageOperationsManager packageOperationsManager;
    private final AppDatabase database;
    private final AppRepository appRepository;
    private final ProfileRepository profileRepository;
    private final HistoryRepository historyRepository;
    private final AppIconLoader appIconLoader;
    private final AppViewModelFactory viewModelFactory;

    public AppContainer(Context context) {
        Context appContext = context.getApplicationContext();
        this.ioExecutor = Executors.newFixedThreadPool(2);
        this.iconExecutor = Executors.newFixedThreadPool(3);
        this.preferencesManager = new AppPreferencesManager(appContext);
        this.safetyGuard = new PackageSafetyGuard(appContext);
        this.shizukuConnectionManager = new ShizukuConnectionManager(appContext);
        this.systemPackageScanner = new SystemPackageScanner(appContext, safetyGuard);
        this.packageOperationsManager = new PackageOperationsManager(
                shizukuConnectionManager,
                systemPackageScanner,
                safetyGuard
        );
        this.database = AppDatabase.getInstance(appContext);
        this.appRepository = new AppRepository(
                systemPackageScanner,
                packageOperationsManager,
                database.appMetadataDao(),
                database.operationHistoryDao(),
                ioExecutor
        );
        this.profileRepository = new ProfileRepository(
                database.appProfileDao(),
                database.operationHistoryDao(),
                systemPackageScanner,
                packageOperationsManager,
                appRepository,
                ioExecutor
        );
        this.historyRepository = new HistoryRepository(
                database.operationHistoryDao(),
                ioExecutor
        );
        this.appIconLoader = new AppIconLoader(appContext, iconExecutor);
        this.viewModelFactory = new AppViewModelFactory(
                appRepository,
                profileRepository,
                historyRepository,
                shizukuConnectionManager,
                preferencesManager,
                ioExecutor
        );
    }

    public ExecutorService getIoExecutor() {
        return ioExecutor;
    }

    public AppPreferencesManager getPreferencesManager() {
        return preferencesManager;
    }

    public PackageSafetyGuard getSafetyGuard() {
        return safetyGuard;
    }

    public ShizukuConnectionManager getShizukuConnectionManager() {
        return shizukuConnectionManager;
    }

    public AppRepository getAppRepository() {
        return appRepository;
    }

    public ProfileRepository getProfileRepository() {
        return profileRepository;
    }

    public HistoryRepository getHistoryRepository() {
        return historyRepository;
    }

    public AppIconLoader getAppIconLoader() {
        return appIconLoader;
    }

    public AppViewModelFactory getViewModelFactory() {
        return viewModelFactory;
    }

    public void shutdown() {
        shizukuConnectionManager.cleanup();
        ioExecutor.shutdown();
        iconExecutor.shutdown();
    }
}

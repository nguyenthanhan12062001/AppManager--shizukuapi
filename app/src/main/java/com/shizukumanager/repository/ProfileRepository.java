package com.shizukumanager.app.repository;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.shizukumanager.app.database.dao.AppProfileDao;
import com.shizukumanager.app.database.dao.OperationHistoryDao;
import com.shizukumanager.app.database.entity.AppProfileEntity;
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import com.shizukumanager.app.database.entity.ProfileAppEntity;
import com.shizukumanager.app.database.entity.SavedAppStateEntity;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.BulkOperationSummary;
import com.shizukumanager.app.model.OperationResult;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.model.ProfileActionType;
import com.shizukumanager.app.model.ProfileWithApps;
import com.shizukumanager.app.pkg.PackageOperationsManager;
import com.shizukumanager.app.pkg.SystemPackageScanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

// Repository quan ly App Profiles, sao luu/khoi phuc trang thai va xuat/nhap JSON
public class ProfileRepository {

    public interface ProfileExecutionCallback {
        void onComplete(BulkOperationSummary summary);
    }

    public interface ProfileProgressCallback {
        void onProgress(int completed, int total, String currentAppName);
    }

    public interface JsonResultCallback {
        void onResult(boolean success, int count, String errorMessage);
    }

    private static class ExportProfileDto {
        String name;
        String description;
        List<ExportProfileAppDto> apps;
    }

    private static class ExportProfileAppDto {
        String packageName;
        String appName;
        String targetAction;
    }

    private final AppProfileDao profileDao;
    private final OperationHistoryDao historyDao;
    private final SystemPackageScanner packageScanner;
    private final PackageOperationsManager operationsManager;
    private final AppRepository appRepository;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final MediatorLiveData<List<ProfileWithApps>> profilesLiveData = new MediatorLiveData<>();
    private List<AppProfileEntity> latestProfiles = Collections.emptyList();
    private List<ProfileAppEntity> latestApps = Collections.emptyList();
    private List<SavedAppStateEntity> latestSavedStates = Collections.emptyList();

    public ProfileRepository(
            AppProfileDao profileDao,
            OperationHistoryDao historyDao,
            SystemPackageScanner packageScanner,
            PackageOperationsManager operationsManager,
            AppRepository appRepository,
            ExecutorService executor
    ) {
        this.profileDao = profileDao;
        this.historyDao = historyDao;
        this.packageScanner = packageScanner;
        this.operationsManager = operationsManager;
        this.appRepository = appRepository;
        this.executor = executor;

        profilesLiveData.addSource(profileDao.observeAllProfiles(), list -> {
            latestProfiles = list != null ? list : Collections.emptyList();
            rebuildProfilesComposite();
        });
        profilesLiveData.addSource(profileDao.observeAllProfileApps(), list -> {
            latestApps = list != null ? list : Collections.emptyList();
            rebuildProfilesComposite();
        });
        profilesLiveData.addSource(profileDao.observeAllSavedStates(), list -> {
            latestSavedStates = list != null ? list : Collections.emptyList();
            rebuildProfilesComposite();
        });
    }

    private void rebuildProfilesComposite() {
        Map<Long, List<ProfileAppEntity>> appsByProfile = new HashMap<>();
        for (ProfileAppEntity item : latestApps) {
            appsByProfile.computeIfAbsent(item.profileId, k -> new ArrayList<>()).add(item);
        }
        Map<Long, List<SavedAppStateEntity>> statesByProfile = new HashMap<>();
        for (SavedAppStateEntity st : latestSavedStates) {
            statesByProfile.computeIfAbsent(st.profileId, k -> new ArrayList<>()).add(st);
        }
        List<ProfileWithApps> combined = new ArrayList<>(latestProfiles.size());
        for (AppProfileEntity profile : latestProfiles) {
            combined.add(new ProfileWithApps(
                    profile,
                    appsByProfile.getOrDefault(profile.id, Collections.emptyList()),
                    statesByProfile.getOrDefault(profile.id, Collections.emptyList())
            ));
        }
        profilesLiveData.setValue(combined);
    }

    public LiveData<List<ProfileWithApps>> observeProfiles() {
        return profilesLiveData;
    }

    public void createProfile(String name, String description) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            AppProfileEntity entity = new AppProfileEntity(name.trim(), description.trim(), now, now);
            profileDao.insertProfile(entity);
        });
    }

    public void renameProfile(AppProfileEntity profile, String newName, String newDescription) {
        executor.execute(() -> {
            profile.name = newName.trim();
            profile.description = newDescription.trim();
            profile.updatedAt = System.currentTimeMillis();
            profileDao.updateProfile(profile);
        });
    }

    public void deleteProfile(AppProfileEntity profile) {
        executor.execute(() -> profileDao.deleteProfile(profile));
    }

    public void updateProfileApps(long profileId, List<ProfileAppEntity> updatedApps) {
        executor.execute(() -> {
            profileDao.clearAppsForProfile(profileId);
            if (updatedApps != null && !updatedApps.isEmpty()) {
                profileDao.insertProfileApps(updatedApps);
            }
            AppProfileEntity profile = profileDao.getProfileByIdSync(profileId);
            if (profile != null) {
                profile.updatedAt = System.currentTimeMillis();
                profileDao.updateProfile(profile);
            }
        });
    }

    // Luu trang thai hien tai truoc khi thay doi va ap dung dung danh sach trong Profile
    public void applyProfile(
            long profileId,
            ProfileProgressCallback progressCallback,
            ProfileExecutionCallback completeCallback
    ) {
        executor.execute(() -> {
            AppProfileEntity profile = profileDao.getProfileByIdSync(profileId);
            List<ProfileAppEntity> profileApps = profileDao.getAppsForProfileSync(profileId);
            if (profile == null || profileApps.isEmpty()) {
                mainHandler.post(() -> {
                    if (completeCallback != null) {
                        completeCallback.onComplete(new BulkOperationSummary(OperationType.ENABLE, Collections.emptyList()));
                    }
                });
                return;
            }

            long snapshotTime = System.currentTimeMillis();
            List<SavedAppStateEntity> snapshots = new ArrayList<>();
            for (ProfileAppEntity entry : profileApps) {
                AppInfoModel current = packageScanner.querySinglePackage(entry.packageName);
                if (current != null) {
                    snapshots.add(new SavedAppStateEntity(
                            profileId,
                            entry.packageName,
                            current.getAppName(),
                            current.isEnabled(),
                            current.isSuspended(),
                            snapshotTime
                    ));
                }
            }
            profileDao.clearSavedStatesForProfile(profileId);
            if (!snapshots.isEmpty()) {
                profileDao.insertSavedStates(snapshots);
            }

            List<OperationResult> results = new ArrayList<>();
            int total = profileApps.size();
            for (int i = 0; i < total; i++) {
                ProfileAppEntity item = profileApps.get(i);
                final int step = i + 1;
                mainHandler.post(() -> {
                    if (progressCallback != null) {
                        progressCallback.onProgress(step, total, item.appName);
                    }
                });
                OperationType op = mapTargetAction(item.targetAction);
                OperationResult res = operationsManager.executeOperation(item.packageName, item.appName, op);
                results.add(res);
                historyDao.insert(toHistoryEntity(res));
            }

            profile.lastAppliedAt = System.currentTimeMillis();
            profileDao.updateProfile(profile);
            appRepository.refreshPackages();

            BulkOperationSummary summary = new BulkOperationSummary(OperationType.DISABLE, results);
            mainHandler.post(() -> {
                if (completeCallback != null) {
                    completeCallback.onComplete(summary);
                }
            });
        });
    }

    // Khoi phuc trang thai cac ung dung da luu truoc do cua Profile
    public void restoreSavedStates(
            long profileId,
            ProfileProgressCallback progressCallback,
            ProfileExecutionCallback completeCallback
    ) {
        executor.execute(() -> {
            List<SavedAppStateEntity> savedStates = profileDao.getSavedStatesForProfileSync(profileId);
            if (savedStates.isEmpty()) {
                mainHandler.post(() -> {
                    if (completeCallback != null) {
                        completeCallback.onComplete(new BulkOperationSummary(OperationType.ENABLE, Collections.emptyList()));
                    }
                });
                return;
            }

            List<OperationResult> results = new ArrayList<>();
            int total = savedStates.size();
            for (int i = 0; i < total; i++) {
                SavedAppStateEntity saved = savedStates.get(i);
                final int step = i + 1;
                mainHandler.post(() -> {
                    if (progressCallback != null) {
                        progressCallback.onProgress(step, total, saved.appName);
                    }
                });
                AppInfoModel current = packageScanner.querySinglePackage(saved.packageName);
                if (current != null) {
                    if (current.isEnabled() != saved.wasEnabled) {
                        OperationType op = saved.wasEnabled ? OperationType.ENABLE : OperationType.DISABLE;
                        OperationResult res = operationsManager.executeOperation(saved.packageName, saved.appName, op);
                        results.add(res);
                        historyDao.insert(toHistoryEntity(res));
                    }
                    if (current.isSuspended() != saved.wasSuspended) {
                        OperationType op = saved.wasSuspended ? OperationType.SUSPEND : OperationType.UNSUSPEND;
                        OperationResult res = operationsManager.executeOperation(saved.packageName, saved.appName, op);
                        results.add(res);
                        historyDao.insert(toHistoryEntity(res));
                    }
                }
            }

            appRepository.refreshPackages();
            BulkOperationSummary summary = new BulkOperationSummary(OperationType.ENABLE, results);
            mainHandler.post(() -> {
                if (completeCallback != null) {
                    completeCallback.onComplete(summary);
                }
            });
        });
    }

    public String exportProfilesToJsonSync() {
        List<AppProfileEntity> allProfiles = profileDao.getAllProfilesSync();
        List<ExportProfileDto> exportList = new ArrayList<>(allProfiles.size());
        for (AppProfileEntity p : allProfiles) {
            ExportProfileDto dto = new ExportProfileDto();
            dto.name = p.name;
            dto.description = p.description;
            dto.apps = new ArrayList<>();
            for (ProfileAppEntity pa : profileDao.getAppsForProfileSync(p.id)) {
                ExportProfileAppDto appDto = new ExportProfileAppDto();
                appDto.packageName = pa.packageName;
                appDto.appName = pa.appName;
                appDto.targetAction = pa.targetAction;
                dto.apps.add(appDto);
            }
            exportList.add(dto);
        }
        return gson.toJson(exportList);
    }

    public void importProfilesFromJson(String json, JsonResultCallback callback) {
        executor.execute(() -> {
            try {
                Type listType = new TypeToken<List<ExportProfileDto>>() {
                }.getType();
                List<ExportProfileDto> imported = gson.fromJson(json, listType);
                if (imported == null) {
                    mainHandler.post(() -> callback.onResult(false, 0, "Invalid JSON format"));
                    return;
                }
                int count = 0;
                long now = System.currentTimeMillis();
                for (ExportProfileDto dto : imported) {
                    if (dto == null || dto.name == null || dto.name.trim().isEmpty()) {
                        continue;
                    }
                    AppProfileEntity profile = new AppProfileEntity(
                            dto.name.trim(),
                            dto.description != null ? dto.description.trim() : "",
                            now,
                            now
                    );
                    long newId = profileDao.insertProfile(profile);
                    if (dto.apps != null && !dto.apps.isEmpty()) {
                        List<ProfileAppEntity> appEntities = new ArrayList<>();
                        for (ExportProfileAppDto appDto : dto.apps) {
                            if (appDto.packageName != null && !appDto.packageName.trim().isEmpty()) {
                                String action = sanitizeAction(appDto.targetAction);
                                appEntities.add(new ProfileAppEntity(
                                        newId,
                                        appDto.packageName.trim(),
                                        appDto.appName != null ? appDto.appName : appDto.packageName.trim(),
                                        action
                                ));
                            }
                        }
                        if (!appEntities.isEmpty()) {
                            profileDao.insertProfileApps(appEntities);
                        }
                    }
                    count++;
                }
                final int importedCount = count;
                mainHandler.post(() -> callback.onResult(true, importedCount, ""));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResult(false, 0, e.getMessage() != null ? e.getMessage() : "JSON parse error"));
            }
        });
    }

    private String sanitizeAction(String raw) {
        if (raw == null) {
            return ProfileActionType.DISABLE.name();
        }
        try {
            return ProfileActionType.valueOf(raw).name();
        } catch (IllegalArgumentException e) {
            return ProfileActionType.DISABLE.name();
        }
    }

    private OperationType mapTargetAction(String action) {
        if (ProfileActionType.SUSPEND.name().equals(action)) {
            return OperationType.FREEZE;
        }
        if (ProfileActionType.ENABLE.name().equals(action)) {
            return OperationType.ENABLE;
        }
        return OperationType.DISABLE;
    }

    private OperationHistoryEntity toHistoryEntity(OperationResult result) {
        return new OperationHistoryEntity(
                result.getPackageName(),
                result.getAppName(),
                result.getOperationType().name(),
                result.getPreviousState().name(),
                result.getNewState().name(),
                result.isSuccess(),
                result.isSuccess() ? "" : (result.getErrorType().name() + (result.getRawErrorDetail().isEmpty() ? "" : ": " + result.getRawErrorDetail())),
                result.getTimestampMillis()
        );
    }
}

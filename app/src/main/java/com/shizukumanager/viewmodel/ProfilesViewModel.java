package com.shizukumanager.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.shizukumanager.app.database.entity.AppProfileEntity;
import com.shizukumanager.app.database.entity.ProfileAppEntity;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.BulkOperationSummary;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.model.ProfileWithApps;
import com.shizukumanager.app.repository.AppRepository;
import com.shizukumanager.app.repository.ProfileRepository;
import java.util.List;

// ViewModel quan ly App Profiles, ap dung va khoi phuc trang thai da luu
public class ProfilesViewModel extends ViewModel {
    private final ProfileRepository profileRepository;
    private final AppRepository appRepository;

    private final MutableLiveData<AppListViewModel.BulkProgressState> progressLiveData = new MutableLiveData<>();
    private final MutableLiveData<BulkOperationSummary> executionSummaryEvent = new MutableLiveData<>();

    public ProfilesViewModel(ProfileRepository profileRepository, AppRepository appRepository) {
        this.profileRepository = profileRepository;
        this.appRepository = appRepository;
    }

    public LiveData<List<ProfileWithApps>> observeProfiles() {
        return profileRepository.observeProfiles();
    }

    public LiveData<List<AppInfoModel>> observeAllApps() {
        return appRepository.observeAllApps();
    }

    public LiveData<AppListViewModel.BulkProgressState> observeProgress() {
        return progressLiveData;
    }

    public LiveData<BulkOperationSummary> observeExecutionSummaryEvent() {
        return executionSummaryEvent;
    }

    public void consumeExecutionSummaryEvent() {
        executionSummaryEvent.setValue(null);
    }

    public void createProfile(String name, String description) {
        profileRepository.createProfile(name, description);
    }

    public void renameProfile(AppProfileEntity profile, String newName, String newDescription) {
        profileRepository.renameProfile(profile, newName, newDescription);
    }

    public void deleteProfile(AppProfileEntity profile) {
        profileRepository.deleteProfile(profile);
    }

    public void updateProfileApps(long profileId, List<ProfileAppEntity> apps) {
        profileRepository.updateProfileApps(profileId, apps);
    }

    public void applyProfile(long profileId, int totalApps) {
        progressLiveData.setValue(new AppListViewModel.BulkProgressState(true, 0, totalApps, "", OperationType.DISABLE));
        profileRepository.applyProfile(
                profileId,
                (completed, total, currentAppName) -> progressLiveData.setValue(
                        new AppListViewModel.BulkProgressState(true, completed, total, currentAppName, OperationType.DISABLE)
                ),
                summary -> {
                    progressLiveData.setValue(new AppListViewModel.BulkProgressState(false, totalApps, totalApps, "", OperationType.DISABLE));
                    executionSummaryEvent.setValue(summary);
                }
        );
    }

    public void restoreProfile(long profileId, int totalApps) {
        progressLiveData.setValue(new AppListViewModel.BulkProgressState(true, 0, totalApps, "", OperationType.ENABLE));
        profileRepository.restoreSavedStates(
                profileId,
                (completed, total, currentAppName) -> progressLiveData.setValue(
                        new AppListViewModel.BulkProgressState(true, completed, total, currentAppName, OperationType.ENABLE)
                ),
                summary -> {
                    progressLiveData.setValue(new AppListViewModel.BulkProgressState(false, totalApps, totalApps, "", OperationType.ENABLE));
                    executionSummaryEvent.setValue(summary);
                }
        );
    }
}

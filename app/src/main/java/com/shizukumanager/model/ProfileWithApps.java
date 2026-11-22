package com.shizukumanager.app.model;

import com.shizukumanager.app.database.entity.AppProfileEntity;
import com.shizukumanager.app.database.entity.ProfileAppEntity;
import com.shizukumanager.app.database.entity.SavedAppStateEntity;
import java.util.Collections;
import java.util.List;

// Lop gom nhom Profile cung danh sach ung dung va trang thai da luu
public class ProfileWithApps {
    private final AppProfileEntity profile;
    private final List<ProfileAppEntity> apps;
    private final List<SavedAppStateEntity> savedStates;

    public ProfileWithApps(
            AppProfileEntity profile,
            List<ProfileAppEntity> apps,
            List<SavedAppStateEntity> savedStates
    ) {
        this.profile = profile;
        this.apps = apps == null ? Collections.emptyList() : apps;
        this.savedStates = savedStates == null ? Collections.emptyList() : savedStates;
    }

    public AppProfileEntity getProfile() {
        return profile;
    }

    public List<ProfileAppEntity> getApps() {
        return apps;
    }

    public List<SavedAppStateEntity> getSavedStates() {
        return savedStates;
    }

    public boolean hasSavedStates() {
        return !savedStates.isEmpty();
    }
}

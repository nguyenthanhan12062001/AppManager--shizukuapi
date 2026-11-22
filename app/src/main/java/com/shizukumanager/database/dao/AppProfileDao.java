package com.shizukumanager.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.shizukumanager.app.database.entity.AppProfileEntity;
import com.shizukumanager.app.database.entity.ProfileAppEntity;
import com.shizukumanager.app.database.entity.SavedAppStateEntity;
import java.util.List;

// DAO quan ly App Profiles, danh sach ung dung trong Profile va trang thai da luu
@Dao
public interface AppProfileDao {
    @Query("SELECT * FROM app_profiles ORDER BY updatedAt DESC")
    LiveData<List<AppProfileEntity>> observeAllProfiles();

    @Query("SELECT * FROM app_profiles ORDER BY updatedAt DESC")
    List<AppProfileEntity> getAllProfilesSync();

    @Query("SELECT * FROM app_profiles WHERE id = :profileId LIMIT 1")
    AppProfileEntity getProfileByIdSync(long profileId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProfile(AppProfileEntity profile);

    @Update
    void updateProfile(AppProfileEntity profile);

    @Delete
    void deleteProfile(AppProfileEntity profile);

    @Query("SELECT * FROM profile_apps")
    LiveData<List<ProfileAppEntity>> observeAllProfileApps();

    @Query("SELECT * FROM profile_apps WHERE profileId = :profileId ORDER BY appName COLLATE NOCASE ASC")
    List<ProfileAppEntity> getAppsForProfileSync(long profileId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProfileApps(List<ProfileAppEntity> apps);

    @Query("DELETE FROM profile_apps WHERE profileId = :profileId")
    void clearAppsForProfile(long profileId);

    @Query("SELECT * FROM saved_app_states")
    LiveData<List<SavedAppStateEntity>> observeAllSavedStates();

    @Query("SELECT * FROM saved_app_states WHERE profileId = :profileId")
    List<SavedAppStateEntity> getSavedStatesForProfileSync(long profileId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSavedStates(List<SavedAppStateEntity> states);

    @Query("DELETE FROM saved_app_states WHERE profileId = :profileId")
    void clearSavedStatesForProfile(long profileId);
}

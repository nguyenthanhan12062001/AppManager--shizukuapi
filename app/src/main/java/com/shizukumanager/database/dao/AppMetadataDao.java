package com.shizukumanager.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.shizukumanager.app.database.entity.AppMetadataEntity;
import java.util.List;

// DAO truy van du lieu metadata ung dung da dong bo
@Dao
public interface AppMetadataDao {
    @Query("SELECT * FROM app_metadata ORDER BY appName COLLATE NOCASE ASC")
    LiveData<List<AppMetadataEntity>> observeAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<AppMetadataEntity> items);

    @Query("DELETE FROM app_metadata WHERE packageName NOT IN (:activePackages)")
    void pruneRemovedPackages(List<String> activePackages);
}

package com.shizukumanager.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import java.util.List;

// DAO truy van va luu tru lich su thao tac he thong
@Dao
public interface OperationHistoryDao {
    @Insert
    long insert(OperationHistoryEntity entry);

    @Query("SELECT * FROM operation_history ORDER BY timestampMillis DESC")
    LiveData<List<OperationHistoryEntity>> observeAll();

    @Query("SELECT * FROM operation_history ORDER BY timestampMillis DESC LIMIT :limit")
    LiveData<List<OperationHistoryEntity>> observeRecent(int limit);

    @Query("DELETE FROM operation_history")
    void clearAll();
}

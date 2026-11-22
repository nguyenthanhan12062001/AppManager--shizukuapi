package com.shizukumanager.app.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.shizukumanager.app.database.dao.AppMetadataDao;
import com.shizukumanager.app.database.dao.AppProfileDao;
import com.shizukumanager.app.database.dao.OperationHistoryDao;
import com.shizukumanager.app.database.entity.AppMetadataEntity;
import com.shizukumanager.app.database.entity.AppProfileEntity;
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import com.shizukumanager.app.database.entity.ProfileAppEntity;
import com.shizukumanager.app.database.entity.SavedAppStateEntity;

// Co so du lieu Room cuc bo luu tru Profile, trang thai da luu, metadata va lich su
@Database(
        entities = {
                AppMetadataEntity.class,
                AppProfileEntity.class,
                ProfileAppEntity.class,
                SavedAppStateEntity.class,
                OperationHistoryEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract AppMetadataDao appMetadataDao();
    public abstract AppProfileDao appProfileDao();
    public abstract OperationHistoryDao operationHistoryDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "shizuku_manager.db"
                            )
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

package com.shizukumanager.app.repository;

import androidx.lifecycle.LiveData;
import com.shizukumanager.app.database.dao.OperationHistoryDao;
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;

// Repository quan ly truy van va xoa lich su thao tac Room
public class HistoryRepository {
    private final OperationHistoryDao historyDao;
    private final ExecutorService executor;

    public HistoryRepository(OperationHistoryDao historyDao, ExecutorService executor) {
        this.historyDao = historyDao;
        this.executor = executor;
    }

    public LiveData<List<OperationHistoryEntity>> observeAllHistory() {
        return historyDao.observeAll();
    }

    public LiveData<List<OperationHistoryEntity>> observeRecentHistory(int limit) {
        return historyDao.observeRecent(limit);
    }

    public void clearAllHistory() {
        executor.execute(historyDao::clearAll);
    }
}

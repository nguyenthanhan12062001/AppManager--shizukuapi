package com.shizukumanager.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import com.shizukumanager.app.repository.HistoryRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ViewModel cho man hinh Lich su thao tac kem bo loc ket qua
public class HistoryViewModel extends ViewModel {

    public enum HistoryFilter {
        ALL,
        SUCCESS_ONLY,
        FAILED_ONLY
    }

    private final HistoryRepository historyRepository;
    private final MediatorLiveData<List<OperationHistoryEntity>> filteredHistoryLiveData = new MediatorLiveData<>();
    private List<OperationHistoryEntity> latestAll = Collections.emptyList();
    private HistoryFilter currentFilter = HistoryFilter.ALL;

    public HistoryViewModel(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
        filteredHistoryLiveData.addSource(historyRepository.observeAllHistory(), list -> {
            latestAll = list != null ? list : Collections.emptyList();
            applyFilter();
        });
    }

    public LiveData<List<OperationHistoryEntity>> observeFilteredHistory() {
        return filteredHistoryLiveData;
    }

    public void setFilter(HistoryFilter filter) {
        this.currentFilter = filter == null ? HistoryFilter.ALL : filter;
        applyFilter();
    }

    public void clearAllHistory() {
        historyRepository.clearAllHistory();
    }

    private void applyFilter() {
        if (currentFilter == HistoryFilter.ALL) {
            filteredHistoryLiveData.setValue(latestAll);
            return;
        }
        List<OperationHistoryEntity> filtered = new ArrayList<>();
        for (OperationHistoryEntity item : latestAll) {
            if (currentFilter == HistoryFilter.SUCCESS_ONLY && item.success) {
                filtered.add(item);
            } else if (currentFilter == HistoryFilter.FAILED_ONLY && !item.success) {
                filtered.add(item);
            }
        }
        filteredHistoryLiveData.setValue(filtered);
    }
}

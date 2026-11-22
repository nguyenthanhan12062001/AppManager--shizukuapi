package com.shizukumanager.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.shizukumanager.app.model.AppFilterOptions;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.BulkOperationSummary;
import com.shizukumanager.app.model.OperationResult;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.repository.AppRepository;
import com.shizukumanager.app.util.AppPreferencesManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// ViewModel dung chung cho All Apps, Disabled Apps va Suspended Apps co ho tro chon nhieu va hang loat
public class AppListViewModel extends ViewModel {

    public enum ListMode {
        ALL,
        DISABLED,
        SUSPENDED
    }

    public static class BulkProgressState {
        public final boolean active;
        public final int completed;
        public final int total;
        public final String currentAppName;
        public final OperationType operationType;

        public BulkProgressState(boolean active, int completed, int total, String currentAppName, OperationType operationType) {
            this.active = active;
            this.completed = completed;
            this.total = total;
            this.currentAppName = currentAppName;
            this.operationType = operationType;
        }
    }

    private final AppRepository appRepository;
    private final AppPreferencesManager preferencesManager;

    private ListMode listMode = ListMode.ALL;
    private String searchQuery = "";
    private AppFilterOptions.TypeFilter typeFilter = AppFilterOptions.TypeFilter.ALL;
    private AppFilterOptions.StateFilter stateFilter = AppFilterOptions.StateFilter.ALL;
    private AppFilterOptions.SortOrder sortOrder = AppFilterOptions.SortOrder.NAME_ASC;
    private boolean defaultFilterApplied = false;

    private final MediatorLiveData<List<AppInfoModel>> filteredAppsLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Set<String>> selectedPackagesLiveData = new MutableLiveData<>(new LinkedHashSet<>());
    private final MutableLiveData<BulkProgressState> bulkProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<BulkOperationSummary> bulkResultEvent = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> singleResultEvent = new MutableLiveData<>();

    private List<AppInfoModel> latestAllApps = Collections.emptyList();

    public AppListViewModel(AppRepository appRepository, AppPreferencesManager preferencesManager) {
        this.appRepository = appRepository;
        this.preferencesManager = preferencesManager;
        filteredAppsLiveData.addSource(appRepository.observeAllApps(), apps -> {
            latestAllApps = apps != null ? apps : Collections.emptyList();
            recomputeFilteredList();
        });
    }

    public void initMode(ListMode mode) {
        if (this.listMode != mode || !defaultFilterApplied) {
            this.listMode = mode;
            if (mode == ListMode.ALL && !defaultFilterApplied) {
                typeFilter = preferencesManager.isShowSystemAppsByDefault()
                        ? AppFilterOptions.TypeFilter.ALL
                        : AppFilterOptions.TypeFilter.USER_ONLY;
                defaultFilterApplied = true;
            }
            recomputeFilteredList();
        }
    }

    public void setInitialStateFilter(AppFilterOptions.StateFilter filter) {
        if (filter != null) {
            this.stateFilter = filter;
            recomputeFilteredList();
        }
    }

    public LiveData<List<AppInfoModel>> observeFilteredApps() {
        return filteredAppsLiveData;
    }

    public LiveData<Boolean> observeLoading() {
        return appRepository.observeLoading();
    }

    public LiveData<Set<String>> observeSelectedPackages() {
        return selectedPackagesLiveData;
    }

    public LiveData<BulkProgressState> observeBulkProgress() {
        return bulkProgressLiveData;
    }

    public LiveData<BulkOperationSummary> observeBulkResultEvent() {
        return bulkResultEvent;
    }

    public LiveData<OperationResult> observeSingleResultEvent() {
        return singleResultEvent;
    }

    public void consumeBulkResultEvent() {
        bulkResultEvent.setValue(null);
    }

    public void consumeSingleResultEvent() {
        singleResultEvent.setValue(null);
    }

    public AppFilterOptions.TypeFilter getTypeFilter() {
        return typeFilter;
    }

    public AppFilterOptions.StateFilter getStateFilter() {
        return stateFilter;
    }

    public AppFilterOptions.SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        recomputeFilteredList();
    }

    public void setTypeFilter(AppFilterOptions.TypeFilter filter) {
        this.typeFilter = filter;
        recomputeFilteredList();
    }

    public void setStateFilter(AppFilterOptions.StateFilter filter) {
        this.stateFilter = filter;
        recomputeFilteredList();
    }

    public void setSortOrder(AppFilterOptions.SortOrder order) {
        this.sortOrder = order;
        recomputeFilteredList();
    }

    public void refreshApps() {
        appRepository.refreshPackages();
    }

    public void toggleSelection(String packageName) {
        Set<String> current = new LinkedHashSet<>(getSelectedSet());
        if (current.contains(packageName)) {
            current.remove(packageName);
        } else {
            current.add(packageName);
        }
        selectedPackagesLiveData.setValue(current);
    }

    public void selectAllVisible() {
        List<AppInfoModel> visible = filteredAppsLiveData.getValue();
        Set<String> next = new LinkedHashSet<>();
        if (visible != null) {
            for (AppInfoModel item : visible) {
                next.add(item.getPackageName());
            }
        }
        selectedPackagesLiveData.setValue(next);
    }

    public void clearSelection() {
        selectedPackagesLiveData.setValue(new LinkedHashSet<>());
    }

    public List<AppInfoModel> getSelectedAppModels() {
        Set<String> selected = getSelectedSet();
        List<AppInfoModel> result = new ArrayList<>();
        for (AppInfoModel app : latestAllApps) {
            if (selected.contains(app.getPackageName())) {
                result.add(app);
            }
        }
        return result;
    }

    public void executeSingleAction(AppInfoModel app, OperationType type) {
        appRepository.executeSingleOperation(app.getPackageName(), app.getAppName(), type, singleResultEvent::setValue);
    }

    public void executeBulkAction(OperationType type) {
        List<AppInfoModel> targets = getSelectedAppModels();
        if (targets.isEmpty()) {
            return;
        }
        bulkProgressLiveData.setValue(new BulkProgressState(true, 0, targets.size(), "", type));
        appRepository.executeBulkOperation(
                targets,
                type,
                (completed, total, currentAppName) -> bulkProgressLiveData.setValue(
                        new BulkProgressState(true, completed, total, currentAppName, type)
                ),
                summary -> {
                    bulkProgressLiveData.setValue(new BulkProgressState(false, targets.size(), targets.size(), "", type));
                    clearSelection();
                    bulkResultEvent.setValue(summary);
                }
        );
    }

    private Set<String> getSelectedSet() {
        Set<String> val = selectedPackagesLiveData.getValue();
        return val != null ? val : Collections.emptySet();
    }

    private void recomputeFilteredList() {
        List<AppInfoModel> filtered = new ArrayList<>();
        for (AppInfoModel app : latestAllApps) {
            if (listMode == ListMode.DISABLED && app.isEnabled()) {
                continue;
            }
            if (listMode == ListMode.SUSPENDED && !app.isSuspended()) {
                continue;
            }
            if (listMode == ListMode.ALL) {
                if (typeFilter == AppFilterOptions.TypeFilter.USER_ONLY && app.isSystemApp()) {
                    continue;
                }
                if (typeFilter == AppFilterOptions.TypeFilter.SYSTEM_ONLY && !app.isSystemApp()) {
                    continue;
                }
                if (stateFilter == AppFilterOptions.StateFilter.ENABLED && (!app.isEnabled() || app.isSuspended())) {
                    continue;
                }
                if (stateFilter == AppFilterOptions.StateFilter.DISABLED && app.isEnabled()) {
                    continue;
                }
                if (stateFilter == AppFilterOptions.StateFilter.SUSPENDED && !app.isSuspended()) {
                    continue;
                }
            }
            if (!searchQuery.isEmpty()) {
                boolean matchName = app.getAppName().toLowerCase(Locale.ROOT).contains(searchQuery);
                boolean matchPkg = app.getPackageName().toLowerCase(Locale.ROOT).contains(searchQuery);
                if (!matchName && !matchPkg) {
                    continue;
                }
            }
            filtered.add(app);
        }

        Comparator<AppInfoModel> comparator;
        if (sortOrder == AppFilterOptions.SortOrder.NAME_DESC) {
            comparator = (a, b) -> b.getAppName().compareToIgnoreCase(a.getAppName());
        } else if (sortOrder == AppFilterOptions.SortOrder.UPDATED_DESC) {
            comparator = (a, b) -> Long.compare(b.getLastUpdateTimeMillis(), a.getLastUpdateTimeMillis());
        } else {
            comparator = (a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName());
        }
        filtered.sort(comparator);
        filteredAppsLiveData.setValue(filtered);
    }
}

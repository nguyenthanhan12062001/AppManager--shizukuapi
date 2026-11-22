package com.shizukumanager.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.OperationResult;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.model.ShizukuStatus;
import com.shizukumanager.app.repository.AppRepository;
import com.shizukumanager.app.shizuku.ShizukuConnectionManager;

// ViewModel cho man hinh Chi tiet ung dung, luon truy van lai trang thai sau moi thao tac
public class AppDetailsViewModel extends ViewModel {
    private final AppRepository appRepository;
    private final ShizukuConnectionManager shizukuManager;

    private final MutableLiveData<AppInfoModel> appInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busyLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<OperationResult> operationResultEvent = new MutableLiveData<>();

    public AppDetailsViewModel(AppRepository appRepository, ShizukuConnectionManager shizukuManager) {
        this.appRepository = appRepository;
        this.shizukuManager = shizukuManager;
    }

    public LiveData<AppInfoModel> observeAppInfo() {
        return appInfoLiveData;
    }

    public LiveData<Boolean> observeBusy() {
        return busyLiveData;
    }

    public LiveData<OperationResult> observeOperationResultEvent() {
        return operationResultEvent;
    }

    public LiveData<ShizukuStatus> observeShizukuStatus() {
        return shizukuManager.observeStatus();
    }

    public void consumeOperationResultEvent() {
        operationResultEvent.setValue(null);
    }

    public void loadPackage(String packageName) {
        appRepository.querySinglePackage(packageName, appInfoLiveData::setValue);
    }

    public void executeAction(OperationType type) {
        AppInfoModel current = appInfoLiveData.getValue();
        if (current == null) {
            return;
        }
        busyLiveData.setValue(true);
        appRepository.executeSingleOperation(
                current.getPackageName(),
                current.getAppName(),
                type,
                result -> {
                    operationResultEvent.setValue(result);
                    appRepository.querySinglePackage(current.getPackageName(), refreshed -> {
                        appInfoLiveData.setValue(refreshed);
                        busyLiveData.setValue(false);
                    });
                }
        );
    }
}

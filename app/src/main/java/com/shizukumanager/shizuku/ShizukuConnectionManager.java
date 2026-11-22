package com.shizukumanager.app.shizuku;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.shizukumanager.app.BuildConfig;
import com.shizukumanager.app.model.ShizukuConnectionState;
import com.shizukumanager.app.model.ShizukuStatus;
import com.shizukumanager.app.shizuku.aidl.IShizukuShellService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import rikka.shizuku.Shizuku;

// Quan ly ket noi, cap quyen, theo doi Binder va UserService cua Shizuku
public class ShizukuConnectionManager {
    public static final String SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api";
    private static final int SHIZUKU_PERMISSION_REQUEST_CODE = 4102;
    private static final String SERVICE_PROCESS_SUFFIX = "shizuku_shell_worker";

    private final Context appContext;
    private final MutableLiveData<ShizukuStatus> statusLiveData = new MutableLiveData<>();
    private final Shizuku.UserServiceArgs userServiceArgs;

    private volatile IShizukuShellService shellService;
    private volatile boolean bindingInProgress = false;
    private volatile CountDownLatch bindLatch;

    private final Shizuku.OnBinderReceivedListener binderReceivedListener = () -> {
        refreshStatus();
        tryBindUserServiceIfReady();
    };

    private final Shizuku.OnBinderDeadListener binderDeadListener = () -> {
        shellService = null;
        bindingInProgress = false;
        refreshStatus();
    };

    private final Shizuku.OnRequestPermissionResultListener permissionResultListener = (requestCode, grantResult) -> {
        if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            refreshStatus();
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                tryBindUserServiceIfReady();
            }
        }
    };

    private final ServiceConnection userServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            shellService = IShizukuShellService.Stub.asInterface(binder);
            bindingInProgress = false;
            CountDownLatch latch = bindLatch;
            if (latch != null) {
                latch.countDown();
            }
            refreshStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            shellService = null;
            bindingInProgress = false;
            refreshStatus();
        }
    };

    public ShizukuConnectionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.userServiceArgs = new Shizuku.UserServiceArgs(
                new ComponentName(appContext.getPackageName(), ShizukuShellService.class.getName())
        )
                .daemon(false)
                .processNameSuffix(SERVICE_PROCESS_SUFFIX)
                .debuggable(BuildConfig.DEBUG)
                .version(1);

        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener);
        Shizuku.addBinderDeadListener(binderDeadListener);
        Shizuku.addRequestPermissionResultListener(permissionResultListener);

        refreshStatus();
        tryBindUserServiceIfReady();
    }

    public LiveData<ShizukuStatus> observeStatus() {
        return statusLiveData;
    }

    public ShizukuStatus getCurrentStatus() {
        ShizukuStatus current = statusLiveData.getValue();
        return current != null ? current : computeCurrentStatus();
    }

    public boolean isShizukuAppInstalled() {
        try {
            appContext.getPackageManager().getPackageInfo(SHIZUKU_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Kiem tra lai toan bo trang thai Shizuku va cap nhat LiveData
    public ShizukuStatus refreshStatus() {
        ShizukuStatus status = computeCurrentStatus();
        statusLiveData.postValue(status);
        return status;
    }

    private ShizukuStatus computeCurrentStatus() {
        if (!isShizukuAppInstalled()) {
            return new ShizukuStatus(ShizukuConnectionState.NOT_INSTALLED, -1, -1, false);
        }
        boolean binderAlive = false;
        try {
            binderAlive = Shizuku.pingBinder();
        } catch (Throwable ignored) {
        }
        if (!binderAlive) {
            return new ShizukuStatus(ShizukuConnectionState.SERVICE_NOT_RUNNING, -1, -1, false);
        }
        int permission = PackageManager.PERMISSION_DENIED;
        try {
            if (!Shizuku.isPreV11()) {
                permission = Shizuku.checkSelfPermission();
            }
        } catch (Throwable ignored) {
        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return new ShizukuStatus(
                    ShizukuConnectionState.PERMISSION_REQUIRED,
                    safeGetShizukuVersion(),
                    -1,
                    false
            );
        }
        int uid = safeGetShizukuUid();
        return new ShizukuStatus(
                ShizukuConnectionState.CONNECTED,
                safeGetShizukuVersion(),
                uid,
                shellService != null && shellService.asBinder().isBinderAlive()
        );
    }

    private int safeGetShizukuVersion() {
        try {
            return Shizuku.getVersion();
        } catch (Throwable t) {
            return -1;
        }
    }

    private int safeGetShizukuUid() {
        try {
            return Shizuku.getUid();
        } catch (Throwable t) {
            return -1;
        }
    }

    public void requestPermission() {
        try {
            if (Shizuku.pingBinder() && !Shizuku.isPreV11()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE);
                } else {
                    tryBindUserServiceIfReady();
                }
            } else {
                refreshStatus();
            }
        } catch (Throwable ignored) {
            refreshStatus();
        }
    }

    public boolean openShizukuApp(Context activityContext) {
        try {
            Intent launchIntent = appContext.getPackageManager().getLaunchIntentForPackage(SHIZUKU_PACKAGE_NAME);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activityContext.startActivity(launchIntent);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public synchronized void tryBindUserServiceIfReady() {
        ShizukuStatus status = computeCurrentStatus();
        if (!status.isReady()) {
            return;
        }
        if (shellService != null && shellService.asBinder().isBinderAlive()) {
            return;
        }
        if (bindingInProgress) {
            return;
        }
        try {
            bindingInProgress = true;
            bindLatch = new CountDownLatch(1);
            Shizuku.bindUserService(userServiceArgs, userServiceConnection);
        } catch (Throwable t) {
            bindingInProgress = false;
            shellService = null;
            refreshStatus();
        }
    }

    // Dam bao ket noi UserService truoc khi gui lenh tu nen
    public IShizukuShellService awaitUserService(long timeoutMs) {
        ShizukuStatus status = refreshStatus();
        if (!status.isReady()) {
            return null;
        }
        if (shellService != null && shellService.asBinder().isBinderAlive()) {
            return shellService;
        }
        tryBindUserServiceIfReady();
        CountDownLatch latch = bindLatch;
        if (latch != null) {
            try {
                latch.await(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return (shellService != null && shellService.asBinder().isBinderAlive()) ? shellService : null;
    }

    public CommandExecutionResult executeShellCommand(String... args) {
        IShizukuShellService service = awaitUserService(4000L);
        if (service == null) {
            return new CommandExecutionResult(-1, "", "Shizuku UserService unavailable", true);
        }
        try {
            String rawJson = service.executeCommand(args);
            JSONObject obj = new JSONObject(rawJson);
            return new CommandExecutionResult(
                    obj.optInt("exitCode", -1),
                    obj.optString("stdout", ""),
                    obj.optString("stderr", ""),
                    false
            );
        } catch (DeadObjectException e) {
            shellService = null;
            refreshStatus();
            return new CommandExecutionResult(-1, "", "Binder dead: " + e.getMessage(), true);
        } catch (RemoteException e) {
            shellService = null;
            refreshStatus();
            return new CommandExecutionResult(-1, "", "Remote error: " + e.getMessage(), true);
        } catch (Exception e) {
            return new CommandExecutionResult(-1, "", e.getMessage(), false);
        }
    }

    public void cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener);
            Shizuku.removeBinderDeadListener(binderDeadListener);
            Shizuku.removeRequestPermissionResultListener(permissionResultListener);
            if (shellService != null) {
                Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true);
                shellService = null;
            }
        } catch (Throwable ignored) {
        }
    }
}

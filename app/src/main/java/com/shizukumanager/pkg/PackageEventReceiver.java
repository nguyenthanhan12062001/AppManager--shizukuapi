package com.shizukumanager.app.pkg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.content.ContextCompat;

// Bo lang nghe su kien thay doi trang thai goi tu he thong hoac ADB ben ngoai
public class PackageEventReceiver extends BroadcastReceiver {

    public interface OnPackageChangeListener {
        void onExternalPackageChanged(String packageName);
    }

    private final OnPackageChangeListener listener;
    private boolean registered = false;

    public PackageEventReceiver(OnPackageChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || listener == null) {
            return;
        }
        String pkg = intent.getData() != null ? intent.getData().getSchemeSpecificPart() : null;
        listener.onExternalPackageChanged(pkg);
    }

    public void register(Context context) {
        if (registered) {
            return;
        }
        Context appCtx = context.getApplicationContext();

        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        pkgFilter.addDataScheme("package");
        ContextCompat.registerReceiver(appCtx, this, pkgFilter, ContextCompat.RECEIVER_EXPORTED);

        IntentFilter suspendFilter = new IntentFilter();
        suspendFilter.addAction(Intent.ACTION_PACKAGES_SUSPENDED);
        suspendFilter.addAction(Intent.ACTION_PACKAGES_UNSUSPENDED);
        ContextCompat.registerReceiver(appCtx, this, suspendFilter, ContextCompat.RECEIVER_EXPORTED);

        registered = true;
    }

    public void unregister(Context context) {
        if (!registered) {
            return;
        }
        try {
            context.getApplicationContext().unregisterReceiver(this);
        } catch (IllegalArgumentException ignored) {
        }
        registered = false;
    }
}

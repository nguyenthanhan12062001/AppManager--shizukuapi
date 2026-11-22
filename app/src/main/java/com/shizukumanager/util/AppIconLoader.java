package com.shizukumanager.app.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.shizukumanager.app.R;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

// Bo tai icon ung dung bat dong bo kem LruCache giup cuon danh sach muot ma
public class AppIconLoader {
    private final PackageManager packageManager;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final LruCache<String, Drawable> iconCache;
    private final Drawable fallbackIcon;

    public AppIconLoader(Context context, ExecutorService executor) {
        this.packageManager = context.getApplicationContext().getPackageManager();
        this.executor = executor;
        this.iconCache = new LruCache<>(160);
        this.fallbackIcon = ContextCompat.getDrawable(context, R.drawable.ic_apps);
    }

    public void loadIcon(String packageName, ImageView imageView) {
        if (imageView == null || packageName == null) {
            return;
        }
        imageView.setTag(packageName);
        Drawable cached = iconCache.get(packageName);
        if (cached != null) {
            imageView.setImageDrawable(cached);
            return;
        }
        imageView.setImageDrawable(fallbackIcon);
        WeakReference<ImageView> targetRef = new WeakReference<>(imageView);
        executor.execute(() -> {
            Drawable loaded;
            try {
                loaded = packageManager.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                loaded = fallbackIcon;
            }
            if (loaded != null) {
                iconCache.put(packageName, loaded);
            }
            final Drawable finalDrawable = loaded != null ? loaded : fallbackIcon;
            mainHandler.post(() -> {
                ImageView target = targetRef.get();
                if (target != null && packageName.equals(target.getTag())) {
                    target.setImageDrawable(finalDrawable);
                }
            });
        });
    }

    public void clearCache() {
        iconCache.evictAll();
    }
}

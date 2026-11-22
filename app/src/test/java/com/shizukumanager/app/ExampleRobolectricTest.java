package com.shizukumanager.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.shizukumanager.app.util.PackageSafetyGuard;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

// Kiem thu Robolectric kiem tra tai nguyen va co che bao ve goi he thong
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public class ExampleRobolectricTest {

    @Test
    public void readStringFromContext_matchesAppName() {
        Context context = ApplicationProvider.getApplicationContext();
        String appName = context.getString(R.string.app_name);
        assertEquals("Shizuku Manager", appName);
    }

    @Test
    public void safetyGuard_protectsSelfAndCriticalSystemPackages() {
        Context context = ApplicationProvider.getApplicationContext();
        PackageSafetyGuard guard = new PackageSafetyGuard(context);
        assertTrue(guard.isSelfPackage(context.getPackageName()));
        assertTrue(guard.isCriticalSystemPackage("android"));
        assertTrue(guard.isCriticalSystemPackage("moe.shizuku.privileged.api"));
    }
}

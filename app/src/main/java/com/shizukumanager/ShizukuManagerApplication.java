package com.shizukumanager.app;

import android.app.Application;
import com.shizukumanager.app.di.AppContainer;

// Lop Application khoi tao Manual DI Container va ap dung ngon ngu da luu
public class ShizukuManagerApplication extends Application {
    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
        appContainer.getPreferencesManager().applySavedLanguageOnStartup();
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}

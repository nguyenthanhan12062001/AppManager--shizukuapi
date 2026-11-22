package com.shizukumanager.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// Quan ly tuy chon hanh vi, giao dien va ngon ngu cua nguoi dung
public class AppPreferencesManager {
    private static final String PREFS_NAME = "shizuku_manager_prefs";
    private static final String KEY_CONFIRM_SINGLE = "confirm_single_action";
    private static final String KEY_AUTO_REFRESH = "auto_refresh_resume";
    private static final String KEY_SHOW_SYSTEM = "show_system_default";
    private static final String KEY_AMBIENT_ORBS = "ambient_orbs_enabled";
    private static final String KEY_LANGUAGE_TAG = "language_tag";

    private final SharedPreferences prefs;
    private final MutableLiveData<Boolean> ambientOrbsLiveData = new MutableLiveData<>();

    public AppPreferencesManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.ambientOrbsLiveData.setValue(isAmbientOrbsEnabled());
    }

    public boolean isConfirmSingleAction() {
        return prefs.getBoolean(KEY_CONFIRM_SINGLE, true);
    }

    public void setConfirmSingleAction(boolean enabled) {
        prefs.edit().putBoolean(KEY_CONFIRM_SINGLE, enabled).apply();
    }

    public boolean isAutoRefreshOnResume() {
        return prefs.getBoolean(KEY_AUTO_REFRESH, true);
    }

    public void setAutoRefreshOnResume(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_REFRESH, enabled).apply();
    }

    public boolean isShowSystemAppsByDefault() {
        return prefs.getBoolean(KEY_SHOW_SYSTEM, false);
    }

    public void setShowSystemAppsByDefault(boolean enabled) {
        prefs.edit().putBoolean(KEY_SHOW_SYSTEM, enabled).apply();
    }

    public boolean isAmbientOrbsEnabled() {
        return prefs.getBoolean(KEY_AMBIENT_ORBS, true);
    }

    public void setAmbientOrbsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AMBIENT_ORBS, enabled).apply();
        ambientOrbsLiveData.postValue(enabled);
    }

    public LiveData<Boolean> observeAmbientOrbsEnabled() {
        return ambientOrbsLiveData;
    }

    public String getLanguageTag() {
        return prefs.getString(KEY_LANGUAGE_TAG, "");
    }

    // Ap dung ngon ngu da chon qua AppCompatDelegate (ho tro dong bo Android 7-15)
    public void setLanguageTag(String languageTag) {
        String safeTag = languageTag == null ? "" : languageTag;
        prefs.edit().putString(KEY_LANGUAGE_TAG, safeTag).apply();
        LocaleListCompat locales = safeTag.isEmpty()
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(safeTag);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    public void applySavedLanguageOnStartup() {
        String tag = getLanguageTag();
        if (!tag.isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
        }
    }
}

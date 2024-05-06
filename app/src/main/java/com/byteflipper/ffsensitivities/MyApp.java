package com.byteflipper.ffsensitivities;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;

public class MyApp extends Application {
    public static MyApp instance = null;

    public static MyApp getInstance() {
        if (instance == null)
            instance = new MyApp();
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setNightMode();
    }

    public void setNightMode() {
        int nightMode = SharedPreferencesUtils.getInteger(this, "nightMode", 0);
        int[] mode = {AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES};
        AppCompatDelegate.setDefaultNightMode(mode[nightMode]);
    }
}
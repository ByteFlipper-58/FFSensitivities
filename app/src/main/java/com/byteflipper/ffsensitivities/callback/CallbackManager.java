package com.byteflipper.ffsensitivities.callback;

import android.app.Activity;

import com.google.android.material.color.DynamicColors;

public class CallbackManager {
    private static DynamicColors.OnAppliedCallback callback;

    public static void setCallback(DynamicColors.OnAppliedCallback cb) {
        callback = cb;
    }

    public static void invokeCallback(Activity activity) {
        if (callback != null) {
            callback.onApplied(activity);
        }
    }
}
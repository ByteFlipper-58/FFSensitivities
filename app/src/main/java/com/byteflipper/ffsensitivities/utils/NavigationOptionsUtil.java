package com.byteflipper.ffsensitivities.utils;

import androidx.annotation.NonNull;
import androidx.navigation.NavOptions;

import com.byteflipper.ffsensitivities.R;

public class NavigationOptionsUtil {
    private static NavOptions navOptions;

    @NonNull
    public static NavOptions getNavOptions() {
        if (navOptions == null) {
            navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(false)
                    .setEnterAnim(R.anim.fragment_open_enter)
                    .setExitAnim(R.anim.fragment_open_exit)
                    .setPopEnterAnim(R.anim.fragment_close_enter)
                    .setPopExitAnim(R.anim.fragment_close_exit)
                    .build();
        }
        return navOptions;
    }
}

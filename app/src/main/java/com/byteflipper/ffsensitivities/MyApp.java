package com.byteflipper.ffsensitivities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.HarmonizedColors;
import com.google.android.material.color.HarmonizedColorsOptions;

import java.util.Date;

public class MyApp extends Application implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private static final String TAG = "MyApplication";
    public static MyApp instance = null;
    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setNightMode();

        MobileAds.initialize(this, initializationStatus -> Log.d(TAG, "AdMob initialized successfully."));

        this.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        appOpenAdManager = new AppOpenAdManager();
    }

    public void setNightMode() {
        int nightMode = SharedPreferencesUtils.getInteger(this, "nightMode", 0);
        int[] mode = {AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES};
        AppCompatDelegate.setDefaultNightMode(mode[nightMode]);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);

        // Загружаем рекламу и показываем, если она доступна, только если currentActivity не null
        if (currentActivity != null) {
            appOpenAdManager.loadAndShowAdIfAvailable(currentActivity, () -> {
                // Действия после завершения показа рекламы
            });
        } else {
            Log.d(TAG, "currentActivity is null in onStart");
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // Обновляем currentActivity только тогда, когда реклама не отображается
        Log.d(TAG, "onActivityStarted: " + activity);
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Показываем рекламу, если она доступна, при возвращении в приложение
        Log.d(TAG, "onActivityResumed: " + activity);
        appOpenAdManager.showAdIfAvailable(activity, () -> {
            // Действия после завершения показа рекламы
        });
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    public void loadAd(@NonNull Activity activity) {
        appOpenAdManager.loadAd(activity);
    }

    public void showAdIfAvailable(@NonNull Activity activity,
                                  @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener);
    }

    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    private class AppOpenAdManager {
        private static final String LOG_TAG = "AppOpenAdManager";
        private static final String AD_UNIT_ID = "ca-app-pub-4346225518624754/1080825292";

        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;
        private long loadTime = 0;

        public AppOpenAdManager() {
        }

        private void loadAd(Context context) {
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            AdRequest request = new AdRequest.Builder().build();
            AppOpenAd.load(
                    context,
                    AD_UNIT_ID,
                    request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd ad) {
                            appOpenAd = ad;
                            isLoadingAd = false;
                            loadTime = new Date().getTime();

                            Log.d(LOG_TAG, "onAdLoaded.");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            isLoadingAd = false;
                            Log.d(LOG_TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                        }
                    });
        }

        private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            long dateDifference = new Date().getTime() - loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * numHours));
        }

        private boolean isAdAvailable() {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
        }

        public void loadAndShowAdIfAvailable(@NonNull final Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
            if (isAdAvailable()) {
                showAdIfAvailable(activity, onShowAdCompleteListener);
            } else {
                loadAd(activity);
            }
        }

        private void showAdIfAvailable(@NonNull final Activity activity,
                                       @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.");
                return;
            }

            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.");
                if (onShowAdCompleteListener != null) {
                    onShowAdCompleteListener.onShowAdComplete();
                }
                return;
            }

            Log.d(LOG_TAG, "Will show ad.");

            // Откладываем показ рекламы на 1 секунду
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (activity.hasWindowFocus()) { // Проверяем, активно ли окно Activity
                    appOpenAd.setFullScreenContentCallback(
                            new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    appOpenAd = null;
                                    isShowingAd = false;

                                    Log.d(LOG_TAG, "onAdDismissedFullScreenContent.");
                                    if (onShowAdCompleteListener != null) {
                                        onShowAdCompleteListener.onShowAdComplete();
                                    }
                                    loadAd(activity); // Загружаем новую рекламу после показа
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    appOpenAd = null;
                                    isShowingAd = false;

                                    Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                                    if (onShowAdCompleteListener != null) {
                                        onShowAdCompleteListener.onShowAdComplete();
                                    }
                                    loadAd(activity); // Загружаем новую рекламу после ошибки
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    Log.d(LOG_TAG, "onAdShowedFullScreenContent.");
                                }
                            });

                    isShowingAd = true;
                    appOpenAd.show(activity);
                } else {
                    Log.d(LOG_TAG, "AppOpenAd not shown: Activity not in focus");
                    if (onShowAdCompleteListener != null) {
                        onShowAdCompleteListener.onShowAdComplete();
                    }
                }
            }, 1000);
        }
    }
}
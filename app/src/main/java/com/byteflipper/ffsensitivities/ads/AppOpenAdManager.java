package com.byteflipper.ffsensitivities.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.lang.ref.WeakReference;
import java.util.Date;

public class AppOpenAdManager {
    private static final String TAG = "AppOpenAdManager";
    // Используй тестовый ID рекламного блока для разработки
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921";
    private static final long MIN_LOAD_INTERVAL = 30000; // 30 секунд

    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;
    private long loadTime = 0;
    private long lastLoadAttemptTime = 0;
    private WeakReference<Activity> currentActivityRef;

    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    public AppOpenAdManager(Context context) {
        // Инициализация MobileAds
        MobileAds.initialize(context, initializationStatus -> Log.d(TAG, "Mobile Ads инициализированы."));
    }

    public void setCurrentActivity(Activity activity) {
        currentActivityRef = new WeakReference<>(activity);
    }

    public boolean isAdShowing() {
        return isShowingAd;
    }

    public void setAdShowing(boolean adShowing) {
        isShowingAd = adShowing;
    }

    public void loadAd(Context context) {
        long currentTime = System.currentTimeMillis();
        // Проверка, нужно ли загружать рекламу
        if (isLoadingAd || isAdAvailable() || (currentTime - lastLoadAttemptTime < MIN_LOAD_INTERVAL)) {
            return;
        }

        lastLoadAttemptTime = currentTime;
        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(context, AD_UNIT_ID, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                appOpenAd = ad;
                isLoadingAd = false;
                loadTime = new Date().getTime();
                Log.d(TAG, "onAdLoaded.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                isLoadingAd = false;
                Log.e(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                new Handler(Looper.getMainLooper()).postDelayed(() -> loadAd(context), 60000); // Перезагрузка рекламы через 60 сек
            }
        });
    }

    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = new Date().getTime() - loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    public void loadAndShowAdIfAvailable(@NonNull final Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        if (isAdAvailable() && !isShowingAd) {
            isShowingAd = true;
            showAdIfAvailable(activity, onShowAdCompleteListener);
        } else {
            loadAd(activity); // Загружаем рекламу, если она недоступна
        }
    }

    public void showAdIfAvailable(@NonNull final Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        if (isShowingAd || !isAdAvailable()) {
            Log.d(TAG, "Ad is either not available or already showing.");
            onShowAdCompleteListener.onShowAdComplete();
            return;
        }

        Log.d(TAG, "Will show ad.");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!activity.isFinishing() && !activity.isDestroyed() && activity.hasWindowFocus()) {
                appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        resetAdState(activity, onShowAdCompleteListener);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        resetAdState(activity, onShowAdCompleteListener);
                        Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "onAdShowedFullScreenContent.");
                    }
                });

                isShowingAd = true;
                appOpenAd.show(activity);
            } else {
                Log.d(TAG, "AppOpenAd not shown: Activity not in focus");
                onShowAdCompleteListener.onShowAdComplete();
            }
        }, 1000);
    }

    private void resetAdState(Activity activity, OnShowAdCompleteListener onShowAdCompleteListener) {
        appOpenAd = null;
        isShowingAd = false;
        loadAd(activity); // Загружаем новую рекламу после показа
        onShowAdCompleteListener.onShowAdComplete();
    }
}
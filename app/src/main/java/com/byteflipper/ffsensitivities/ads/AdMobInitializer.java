package com.byteflipper.ffsensitivities.ads;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.byteflipper.ffsensitivities.BuildConfig;
import com.byteflipper.ffsensitivities.R;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdMobInitializer {

    // ** Константы для AdMob **
    private static final String BANNER_AD_UNIT_ID = "";
    private static final String APP_OPEN_AD_UNIT_ID = "";
    private static final String TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921";

    private static final String TEST_DEVICE_ID = "";
    private static final String TEST_DEVICE_HASHED_ID = "";

    // ** Остальные константы **
    private static final String TAG = "AdMobInitializer";
    private AppOpenAd appOpenAd;
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LAST_APP_OPEN_AD_TIME = "last_app_open_ad_time";
    private static final long APP_OPEN_AD_INTERVAL_DEBUG = 15 * 1000; // 15 seconds in milliseconds
    private static final long APP_OPEN_AD_INTERVAL_RELEASE = 5 * 60 * 1000; // 5 minutes in milliseconds
    private long appOpenAdInterval = APP_OPEN_AD_INTERVAL_RELEASE; // Default to release interval

    private boolean isPersonalizedAdsAllowed = false;
    private AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    private ScheduledExecutorService scheduler;

    public void initialize(Context context) {
        // Determine if it's a debug build
        boolean isDebugBuild = BuildConfig.DEBUG;
        if (isDebugBuild) {
            appOpenAdInterval = APP_OPEN_AD_INTERVAL_DEBUG;
        }

        // Create a ConsentRequestParameters object.
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();

        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(context);
        consentInformation.requestConsentInfoUpdate(
                (Activity) context,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        (Activity) context,
                        formError -> {
                            if (formError != null) {
                                // Consent gathering failed.
                                Log.w(TAG, String.format("%s: %s",
                                        formError.getErrorCode(),
                                        formError.getMessage()));
                            }

                            // Consent has been gathered.
                            if (consentInformation.canRequestAds()) {
                                initializeMobileAdsSdk(context);
                            }
                        }
                ),
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(FormError formError) {
                        // Consent gathering failed.
                        Log.w(TAG, String.format("%s: %s",
                                formError.getErrorCode(),
                                formError.getMessage()));
                    }
                });

        // Check if you can initialize the Google Mobile Ads SDK in parallel
        // while checking for new consent information. Consent obtained in
        // the previous session can be used to request ads.
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk(context);
        }
    }

    private void initializeMobileAdsSdk(Context context) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(context, initializationStatus -> {
            // Test configuration
            RequestConfiguration.Builder configBuilder = new RequestConfiguration.Builder()
                    .setTestDeviceIds(Arrays.asList(TEST_DEVICE_ID));
            if (BuildConfig.DEBUG) {
                configBuilder.setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE);
            }
            MobileAds.setRequestConfiguration(configBuilder.build());

            // Load ads based on personalization preference
            loadAds(context, BuildConfig.DEBUG);

            // Initialize UMP
            requestUMPConsentInfoUpdate(context, BuildConfig.DEBUG);

            // Schedule app open ad in debug builds
            if (BuildConfig.DEBUG) {
                scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.scheduleWithFixedDelay(() -> showAppOpenAd((Activity) context),
                        appOpenAdInterval, appOpenAdInterval, TimeUnit.MILLISECONDS);
            }
        });
    }

    private void loadAds(Context context, boolean isDebugBuild) {
        loadBannerAd(context, isDebugBuild);
        loadAppOpenAd(context, isDebugBuild);
    }

    private void loadBannerAd(Context context, boolean isDebugBuild) {
        AdView adView = ((Activity) context).findViewById(R.id.banner_ad_view);
        AdRequest adRequest = buildAdRequest(isDebugBuild);
        adView.loadAd(adRequest);
    }

    private void loadAppOpenAd(Context context, boolean isDebugBuild) {
        String adUnitId = isDebugBuild ? TEST_APP_OPEN_AD_UNIT_ID : APP_OPEN_AD_UNIT_ID;
        AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(AppOpenAd ad) {
                Log.d(TAG, "App open ad loaded");
                appOpenAd = ad;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.d(TAG, "App open ad failed to load: " + loadAdError.getMessage());
            }
        };

        AppOpenAd.load(context, adUnitId, buildAdRequest(isDebugBuild),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    public void showAppOpenAd(Activity activity) {
        if (shouldShowAppOpenAd(activity)) {
            if (appOpenAd != null) {
                FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        appOpenAd = null;
                        loadAppOpenAd(activity, BuildConfig.DEBUG);
                        saveLastAppOpenAdTime(activity);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError error) {
                        Log.d(TAG, "App open ad failed to show: " + error.getMessage());
                        appOpenAd = null;
                        loadAppOpenAd(activity, BuildConfig.DEBUG);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "App open ad showed");
                    }
                };

                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                appOpenAd.show(activity);
            } else {
                Log.d(TAG, "App open ad not ready yet");
            }
        }
    }

    private boolean shouldShowAppOpenAd(Context context) {
        long lastAdTime = getLastAppOpenAdTime(context);
        long currentTime = System.currentTimeMillis();
        return currentTime - lastAdTime >= appOpenAdInterval;
    }

    private long getLastAppOpenAdTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_APP_OPEN_AD_TIME, 0);
    }

    private void saveLastAppOpenAdTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_LAST_APP_OPEN_AD_TIME, System.currentTimeMillis());
        editor.apply();
    }

    private void requestUMPConsentInfoUpdate(Context context, boolean isDebugBuild) {
        ConsentDebugSettings.Builder debugSettingsBuilder = new ConsentDebugSettings.Builder(context)
                .addTestDeviceHashedId(TEST_DEVICE_HASHED_ID);

        // Set debug geography only for debug builds
        if (isDebugBuild) {
            debugSettingsBuilder.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA);
        }
        ConsentDebugSettings debugSettings = debugSettingsBuilder.build();

        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setConsentDebugSettings(debugSettings)
                .setTagForUnderAgeOfConsent(false)
                .build();

        UserMessagingPlatform.getConsentInformation(context)
                .requestConsentInfoUpdate(
                        (Activity) context,
                        params,
                        () -> {
                            ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(context);
                            isPersonalizedAdsAllowed = consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED;

                            if (!consentInformation.canRequestAds()) {
                                loadAndShowUMPForm(context, isDebugBuild);
                            }
                        },
                        formError -> Log.d(TAG, "UMP request failed: " + formError.getMessage()));
    }

    private void loadAndShowUMPForm(Context context, boolean isDebugBuild) {
        UserMessagingPlatform.loadConsentForm(
                context,
                consentForm -> consentForm.show(
                        (Activity) context,
                        formError -> {
                            if (formError != null) {
                                Log.d(TAG, "UMP form dismissed with error: " + formError.getMessage());
                            }
                            requestUMPConsentInfoUpdate(context, isDebugBuild);
                        }),
                formError -> Log.d(TAG, "UMP form failed to load: " + formError.getMessage()));
    }

    private AdRequest buildAdRequest(boolean isDebugBuild) {
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        if (isDebugBuild || isPersonalizedAdsAllowed) {
            adRequestBuilder.setHttpTimeoutMillis(30000);
        } else {
            // Request non-personalized ads
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }
        return adRequestBuilder.build();
    }
}
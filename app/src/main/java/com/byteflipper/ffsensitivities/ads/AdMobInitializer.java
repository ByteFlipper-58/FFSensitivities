package com.byteflipper.ffsensitivities.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.byteflipper.ffsensitivities.BuildConfig;
import com.byteflipper.ffsensitivities.R;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdMobInitializer {

    // ** Константы для AdMob **
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-4346225518624754/7047467848";
    private static final String TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    private static final String TEST_DEVICE_ID = "3C2DFEFB9C4FCD9821E061B1C51E049C";
    private static final String TEST_DEVICE_HASHED_ID = "3C2DFEFB9C4FCD9821E061B1C51E049C";

    private static final String TAG = "AdMobInitializer";

    private boolean isPersonalizedAdsAllowed = false;
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    public void initialize(Context context) {
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
        });
    }

    private void loadAds(Context context, boolean isDebugBuild) {
        loadBannerAd(context, isDebugBuild);
    }

    private void loadBannerAd(Context context, boolean isDebugBuild) {
        AdView adView = ((Activity) context).findViewById(R.id.banner_ad_view);
        AdRequest adRequest = buildAdRequest(isDebugBuild);
        adView.loadAd(adRequest);
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
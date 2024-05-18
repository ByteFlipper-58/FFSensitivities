package com.byteflipper.ffsensitivities.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

public class UMPConsentHelper {

    private static final String TAG = "UMPConsentHelper";

    public interface ConsentStatusCallback {
        void onConsentInfoUpdated(ConsentInformation consentInformation, boolean loadConsentForm);
        void onConsentFormLoadFailure(FormError formError);
        void onConsentFormLoadSuccess(ConsentForm consentForm);
    }

    public static void requestConsentInfoUpdate(Context context, ConsentStatusCallback callback) {
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("")
                .build();

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

                            // Используйте canRequestAds() вместо isConsentFormPersonalized() и isConsentRequired()
                            boolean loadConsentForm = !consentInformation.canRequestAds();

                            callback.onConsentInfoUpdated(consentInformation, loadConsentForm);
                        },
                        formError -> callback.onConsentFormLoadFailure(formError));
    }

    public static void loadConsentForm(Context context, ConsentStatusCallback callback) {
        UserMessagingPlatform.loadConsentForm(
                context,
                consentForm -> callback.onConsentFormLoadSuccess(consentForm),
                formError -> callback.onConsentFormLoadFailure(formError));
    }
}
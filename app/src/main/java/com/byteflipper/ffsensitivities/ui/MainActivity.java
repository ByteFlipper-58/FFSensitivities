package com.byteflipper.ffsensitivities.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.byteflipper.ffsensitivities.MyApplication;
import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.ads.GoogleMobileAdsConsentManager;
import com.byteflipper.ffsensitivities.databinding.ActivityMainBinding;
import com.byteflipper.ffsensitivities.interfaces.ProgressIndicatorListener;
import com.byteflipper.ffsensitivities.manager.LanguageManager;
import com.byteflipper.ffsensitivities.manager.ManufacturersManager;
import com.byteflipper.ffsensitivities.utils.AppUpdateHelper;
import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements AppUpdateHelper.UpdateListener, ProgressIndicatorListener {
    private LinearProgressIndicator progressIndicator;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private AppUpdateHelper appUpdateHelper;

    private static final String LOG_TAG = "MainActivity";

    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private final AtomicBoolean gatherConsentFinished = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;

    /**
     * Number of milliseconds to count down before showing the app open ad. This simulates the time
     * needed to load the app.
     */
    private static final long COUNTER_TIME_MILLISECONDS = 5000;

    private long secondsRemaining;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (SharedPreferencesUtils.getBoolean(this, "useDynamicColors"))
            DynamicColors.applyToActivityIfAvailable(this);

        DynamicColors.OnAppliedCallback callback = activity -> {
            setStatusAndNavigationBarColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, SurfaceColors.SURFACE_2.getColor(this)));
        };

        callback.onApplied(this);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            LanguageManager.loadLocale(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        final View content = findViewById(android.R.id.content);
        ManufacturersManager.getInstance().updateAdapterData(this);
        content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (ManufacturersManager.getInstance().isReady()) {
                    content.getViewTreeObserver().removeOnPreDrawListener(this);

                    if (!SharedPreferencesUtils.getBoolean(MainActivity.this, "isFirstOpen")) {
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        progressIndicator = binding.progressIndicator;

        appUpdateHelper = new AppUpdateHelper(this, this);
        appUpdateHelper.checkForAppUpdate();

        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());

        createTimer();

        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                this,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(
                                LOG_TAG,
                                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    gatherConsentFinished.set(true);

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                    if (secondsRemaining <= 0) {
                        //startMainActivity();
                    }
                });

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        binding.bannerAdView.loadAd(adRequest);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            } else {
                Log.d("MainActivity", "Разрешение на отправку уведомлений предоставлено");
            }
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.bottomAppBar, navController);
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Разрешение на отправку уведомлений предоставлено");
            } else {
                Log.d("MainActivity", "Разрешение на отправку уведомлений не предоставлено");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            NavDestination currentDestination = navController.getCurrentDestination();
            if (currentDestination != null && currentDestination.getId() == R.id.settingsFragment) {
                return true;
            }
            navController.navigate(R.id.settingsFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setStatusAndNavigationBarColor(int color) {
        Window window = getWindow();

        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(true);
        }
    }

    @Override
    public void onUpdateCheck() {
        Log.e("AppUpdateHelper", "onUpdateCheck");
    }

    @Override
    public void onUpdateNotAvailable() {
        Log.e("AppUpdateHelper", "onUpdateNotAvailable");
    }

    @Override
    public void onUpdateAvailable() {
        //appUpdateHelper.startImmediateUpdate();
        Log.e("AppUpdateHelper", "onUpdateAvailable");
    }

    @Override
    public void onUpdateDownloadStarted() {
        Log.e("AppUpdateHelper", "onUpdateDownloadStarted");
    }

    @Override
    public void onDownloadProgress(float progress) {
        //
    }

    @Override
    public void onUpdateDownloaded() {
        Log.e("AppUpdateHelper", "onUpdateDownloaded");
    }

    @Override
    public void onUpdateFailed() {
        Log.e("AppUpdateHelper", "onUpdateFailed");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        appUpdateHelper.onActivityResult(requestCode, resultCode);
    }

    /** Create the countdown timer, which counts down to zero and show the app open ad. */
    private void createTimer() {

        CountDownTimer countDownTimer =
                new CountDownTimer(COUNTER_TIME_MILLISECONDS, 1000) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTick(long millisUntilFinished) {
                        secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1;
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFinish() {
                        secondsRemaining = 0;

                        Application application = getApplication();
                        ((MyApplication) application)
                                .showAdIfAvailable(
                                        MainActivity.this,
                                        (MyApplication.OnShowAdCompleteListener) () -> {
                                            // Check if the consent form is currently on screen before moving to the
                                            // main activity.
                                            if (gatherConsentFinished.get()) {
                                                //startMainActivity();
                                            }
                                        });
                    }
                };
        countDownTimer.start();
    }

    private void initializeMobileAdsSdk() {if (isMobileAdsInitializeCalled.getAndSet(true)) {return;}

        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setTestDeviceIds(Arrays.asList(MyApplication.TEST_DEVICE_HASHED_ID))
                        .build());

        new Thread(() -> {
                    MobileAds.initialize(this, initializationStatus -> {});
                    runOnUiThread(() -> {
                        Application application = getApplication();
                        ((MyApplication) application).loadAd(this);
                    });
        }).start();
    }

    @Override
    public void showProgress() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideProgress() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
    }

}
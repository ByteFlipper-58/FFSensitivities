package com.byteflipper.ffsensitivities.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
import com.byteflipper.ffsensitivities.utils.InAppReviewHelper;
import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.tasks.Task;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements ProgressIndicatorListener {
    private NavController navController;

    private LinearProgressIndicator progressIndicator;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private static final String LOG_TAG = "MainActivity";
    private static final int REQUEST_UPDATE = 100;
    private AppUpdateManager appUpdateManager;

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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUpdateManager = AppUpdateManagerFactory.create(this);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (SharedPreferencesUtils.getBoolean(this, "useDynamicColors"))
            DynamicColors.applyToActivityIfAvailable(this);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            LanguageManager.loadLocale(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);

            WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            insetsController.setAppearanceLightNavigationBars(true);

            TypedValue value = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.isLightTheme, value, true);
            boolean isLightTheme = value.data != 0;
            insetsController.setAppearanceLightStatusBars(isLightTheme);

            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        }

        InAppReviewHelper.getInstance(this).requestReviewInfo();
        checkUpdate();

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

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.bottomAppBar, navController);
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

        if (getIntent().getBooleanExtra("openSettingsFragment", false)) {
            navController.navigate(R.id.settingsFragment);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e(LOG_TAG, "Процесс обновления завершился с ошибкой! Код результата: " + resultCode);
            }
        }
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

    private void checkUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            REQUEST_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(LOG_TAG, "Ошибка запуска процесса обновления", e);
                }
            }
        });
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
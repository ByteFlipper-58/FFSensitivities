package com.byteflipper.ffsensitivities.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.ads.AdMobInitializer;
import com.byteflipper.ffsensitivities.ads.UMPConsentHelper;
import com.byteflipper.ffsensitivities.databinding.ActivityMainBinding;
import com.byteflipper.ffsensitivities.interfaces.IScrollHelper;
import com.byteflipper.ffsensitivities.manager.LanguageManager;
import com.byteflipper.ffsensitivities.manager.ManufacturersManager;
import com.byteflipper.ffsensitivities.manager.SensitivitiesManager;
import com.byteflipper.ffsensitivities.utils.AppUpdateHelper;
import com.byteflipper.ffsensitivities.utils.NavigationOptionsUtil;
import com.byteflipper.ffsensitivities.utils.PerAppLanguageManager;
import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.FormError;

public class MainActivity extends AppCompatActivity implements AppUpdateHelper.UpdateListener {
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private ActivityMainBinding binding;

    private AppUpdateHelper appUpdateHelper;
    private Handler handler = new Handler();

    private AdMobInitializer adMobInitializer;

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

        appUpdateHelper = new AppUpdateHelper(this, this);
        appUpdateHelper.checkForAppUpdate();

        adMobInitializer = new AdMobInitializer();
        adMobInitializer.initialize(this);

        UMPConsentHelper.requestConsentInfoUpdate(this, new UMPConsentHelper.ConsentStatusCallback() {
            @Override
            public void onConsentInfoUpdated(ConsentInformation consentInformation, boolean loadConsentForm) {
                if (loadConsentForm) {
                    // Загрузите и покажите форму согласия
                    UMPConsentHelper.loadConsentForm(MainActivity.this, this);
                } else {
                    // Обработка согласия пользователя (если нужно)
                    Log.d("MainActivity", "Consent status: " + consentInformation.getConsentStatus());
                }
            }

            @Override
            public void onConsentFormLoadFailure(FormError formError) {
                // Обработка ошибки загрузки формы согласия
                Log.e("MainActivity", "UMP form load failed: " + formError.getMessage());
            }

            @Override
            public void onConsentFormLoadSuccess(ConsentForm consentForm) {
                // Отображение формы согласия
                consentForm.show(MainActivity.this, new ConsentForm.OnConsentFormDismissedListener() {
                    @Override
                    public void onConsentFormDismissed(@Nullable FormError formError) {
                        // Обработка закрытия формы согласия (если нужно)
                        if (formError != null) {
                            Log.e("MainActivity", "UMP form dismissed with error: " + formError.getMessage());
                        }
                    }
                });
            }
        });

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.bottomAppBar, navController);
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);
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
        appUpdateHelper.startImmediateUpdateFromOutside();
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

    private void setSubtitleAndDisappear(String subtitle, int visibility) {
        binding.progressIndicator.setVisibility(visibility);
        binding.toolbar.setSubtitle(subtitle);

        handler.postDelayed(() -> {
            binding.progressIndicator.setVisibility(View.GONE);
            binding.toolbar.setSubtitle(null);
        }, 2000); // 2000 миллисекунд = 2 секунды
    }
}
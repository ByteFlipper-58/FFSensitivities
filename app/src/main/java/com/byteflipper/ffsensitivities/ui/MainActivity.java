package com.byteflipper.ffsensitivities.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.color.DynamicColors;
import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.databinding.ActivityMainBinding;
import com.byteflipper.ffsensitivities.manager.ManufacturerManager;
import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.SurfaceColors;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private ActivityMainBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (SharedPreferencesUtils.getBoolean(this, "useDynamicColors"))
            DynamicColors.applyToActivityIfAvailable(this);

        DynamicColors.OnAppliedCallback callback = activity -> {
            setStatusBarColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, SurfaceColors.SURFACE_2.getColor(this)));
            setNavigationBarColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, SurfaceColors.SURFACE_2.getColor(this)));
        };

        callback.onApplied(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        final View content = findViewById(android.R.id.content);
        ManufacturerManager.getInstance().updateAdapterData(this);
        content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (ManufacturerManager.getInstance().isReady()) {
                    content.getViewTreeObserver().removeOnPreDrawListener(this);

                    if (SharedPreferencesUtils.getBoolean(MainActivity.this, "isFirstOpen")) {
                        return true;
                    } else {
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        startActivity(intent);
                        finish();
                        return true;
                    }
                } else {
                    return false;
                }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
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

    private void setStatusBarColor(int color) {
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setStatusBarColor(color);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.setStatusBarContrastEnforced(true);
            }
        }
    }

    private void setNavigationBarColor(int color) {
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setNavigationBarColor(color);
        }
    }
}
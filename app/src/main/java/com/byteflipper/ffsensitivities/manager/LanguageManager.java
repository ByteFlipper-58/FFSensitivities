package com.byteflipper.ffsensitivities.manager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import android.content.SharedPreferences;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LanguageManager {

    private static final String TAG = "LanguageManager";
    private static final String PREFS_NAME = "language_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final List<String> SUPPORTED_LANGUAGE_CODES = Arrays.asList(
            "en", "be", "de", "fr", "pl", "ru", "tr", "uk" // Добавьте нужные коды языков
    );

    public static List<String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGE_CODES;
    }

    public static String getLanguageDisplayName(String languageCode) {
        Locale locale = new Locale(languageCode);
        return locale.getDisplayLanguage(locale);
    }

    public static void showLanguageDialog(Context context) {
        String[] languageNames = new String[SUPPORTED_LANGUAGE_CODES.size()];
        for (int i = 0; i < SUPPORTED_LANGUAGE_CODES.size(); i++) {
            languageNames[i] = getLanguageDisplayName(SUPPORTED_LANGUAGE_CODES.get(i));
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("Выберите язык")
                .setItems(languageNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedLanguageCode = SUPPORTED_LANGUAGE_CODES.get(which);
                        setLanguage(context, selectedLanguageCode);
                    }
                })
                .show();
    }

    public static void setLanguage(Context context, String languageCode) {
        if (!SUPPORTED_LANGUAGE_CODES.contains(languageCode)) {
            Log.w(TAG, "Language not supported: " + languageCode);
            return;
        }

        // Сохраняем выбранный язык в SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();

        // Обновляем локаль приложения
        updateLocale(context, languageCode);
    }

    public static void loadLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String languageCode = prefs.getString(KEY_LANGUAGE, "");

        if (!languageCode.isEmpty()) {
            updateLocale(context, languageCode);
        }
    }

    private static void updateLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Синхронизируем локаль для Per-App Language API, если доступно
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && AppCompatDelegate.getApplicationLocales() != null) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale));
            Log.i(TAG, "Per-App Language synchronized to: " + locale.toLanguageTag());
        }
    }

    public static String getCurrentLanguage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: получить локаль из Per-App Language API
            LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
            if (appLocales != null && !appLocales.isEmpty()) {
                return appLocales.get(0).getDisplayName();
            }
        }

        // Android 12 и ниже: получить локаль из Configuration
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        return config.getLocales().get(0).getDisplayName();
    }
}
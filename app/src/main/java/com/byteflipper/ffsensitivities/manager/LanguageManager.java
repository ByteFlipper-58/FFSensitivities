package com.byteflipper.ffsensitivities.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class LanguageManager {

    private static final String PREF_LANGUAGE = "pref_language";
    private String[] languages = {
            "English",
            "Belarusian",
            "German",
            "French",
            "Polish",
            "Russian",
            "Turkish",
            "Ukrainian"
    };

    private String[] languageCodes = {
            "en",
            "be_BY",
            "de",
            "fr",
            "pl",
            "ru",
            "tr",
            "uk"
    };

    public void setSystemLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedLanguage = prefs.getString(PREF_LANGUAGE, "");
        if (!savedLanguage.isEmpty()) {
            setAppLanguage(context, savedLanguage);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                // Получаем поддерживаемые языки приложения
                LocaleListCompat supportedLocales = AppCompatDelegate.getApplicationLocales();
                if (supportedLocales.size() > 0) {
                    Locale systemLocale = supportedLocales.get(0);
                    updateAppLanguage(context, systemLocale);
                } else {
                    setAppLanguage(context, "en");
                }
            } else {
                Locale systemLocale;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    systemLocale = context.getResources().getConfiguration().getLocales().get(0);
                } else {
                    systemLocale = context.getResources().getConfiguration().locale;
                }

                updateAppLanguage(context, systemLocale);
            }
        }
    }

    private void updateAppLanguage(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public void setAppLanguage(Context context, String language) {
        String languageCode = getLanguageCode(language);
        if (languageCode != null) {
            Locale locale = new Locale(languageCode);
            updateAppLanguage(context, locale);

            SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_LANGUAGE, languageCode);
            editor.apply();

            if (context instanceof Activity) {
                ((Activity) context).recreate();
            }
        } else {
            // Обработка неподдерживаемого языка
            Log.e("LanguageManager", "Unsupported language: " + language);
        }
    }

    // Метод для получения кода языка по его названию
    private String getLanguageCode(String language) {
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equalsIgnoreCase(language)) {
                return languageCodes[i];
            }
        }
        return null; // Неизвестный язык
    }

    // Метод для отображения диалогового окна выбора языка
    public void showLanguageDialog(final Context context) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Choose Language")
                .setItems(languages, (dialog, which) -> {
                    String selectedLanguage = languages[which];
                    setAppLanguage(context, selectedLanguage);
                    Log.d("LanguageManager", "Selected language: " + selectedLanguage);
                    Log.d("LanguageManager", "Get language code: " + languages[which]);
                    Log.d("LanguageManager", "App language: " + getAppLanguage(context));
                    Toast.makeText(context, "This feature is still in development and may not work correctly.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // Метод для получения текущего языка приложения
    public String getAppLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString(PREF_LANGUAGE, "en");
    }
}
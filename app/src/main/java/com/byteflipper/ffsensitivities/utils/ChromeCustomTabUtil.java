package com.byteflipper.ffsensitivities.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;

public class ChromeCustomTabUtil {

    public static class Builder {

        private final Context context;
        private final String url;
        private int toolbarColor;

        public Builder(Context context, String url) {
            this.context = context;
            this.url = url;
        }

        public Builder setToolbarColor(int toolbarColor) {
            this.toolbarColor = toolbarColor;
            return this;
        }

        public void open() {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

                CustomTabColorSchemeParams defaultColors = new CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(toolbarColor)
                        .build();
                builder.setDefaultColorSchemeParams(defaultColors);
                builder.setUrlBarHidingEnabled(true);
                builder.setShowTitle(true);
                builder.build();

                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(context, Uri.parse(url));
                customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,
                        Uri.parse("android-app://" + context.getPackageName()));
            } else {
                Toast.makeText(context, "No browser available to open link", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
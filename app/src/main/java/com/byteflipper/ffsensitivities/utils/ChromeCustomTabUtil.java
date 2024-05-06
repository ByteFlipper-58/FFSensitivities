package com.byteflipper.ffsensitivities.utils;

import android.content.Context;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;

public class ChromeCustomTabUtil {

    public static class Builder {

        private Context context;
        private String url;
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
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));

            // Set tab color
            CustomTabColorSchemeParams defaultColors = new CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(toolbarColor)
                    .build();
            builder.setDefaultColorSchemeParams(defaultColors);
        }
    }
}
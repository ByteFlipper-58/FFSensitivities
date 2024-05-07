package com.byteflipper.ffsensitivities.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.byteflipper.ffsensitivities.BuildConfig;
import com.byteflipper.ffsensitivities.R;

public class FeedbackHelper {

    public static void sendEmail(Context context) {
        String deviceInfo = getDeviceInfo(context);
        String appInfo = getAppInfo(context);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"byteflipper.business@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.bug_report_title));
        intent.putExtra(Intent.EXTRA_TEXT, appInfo + "\n\n" + deviceInfo);

        try {
            context.startActivity(Intent.createChooser(intent, ""));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.not_installed_email_client, Toast.LENGTH_SHORT).show();
        }
    }

    private static String getDeviceInfo(Context context) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String androidVersion = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidVersion = Build.VERSION.RELEASE_OR_CODENAME;
        }
        int sdkInt = Build.VERSION.SDK_INT;

        return "Device Info:\n" +
                "Manufacturer: " + manufacturer + "\n" +
                "Model: " + model + "\n" +
                "Android Version: " + androidVersion + " (API " + sdkInt + ")\n" +
                "Connection Type: " + getNetworkClass(context);
    }

    private static String getAppInfo(Context context) {
        return "\n\n\n" +
                "App Info:\n" +
                context.getString(R.string.app_name) + "\n" +
                BuildConfig.APPLICATION_ID + "\n" +
                "App Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\n" +
                "Build Type: " + BuildConfig.BUILD_TYPE;
    }

    private static String getNetworkClass(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info == null || !info.isConnected())
            return "Not Connected"; // not connected

        if (info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";

        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                case TelephonyManager.NETWORK_TYPE_GSM:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                case TelephonyManager.NETWORK_TYPE_IWLAN:
                case 19: // LTE_CA
                    return "4G";
                case TelephonyManager.NETWORK_TYPE_NR:
                    return "5G";
                default:
                    return "Unknown";
            }
        }
        return "?";
    }
}
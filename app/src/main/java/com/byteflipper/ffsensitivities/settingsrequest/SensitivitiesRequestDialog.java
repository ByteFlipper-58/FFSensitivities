package com.byteflipper.ffsensitivities.settingsrequest;

import android.content.Context;
import android.os.Build;

import com.byteflipper.ffsensitivities.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SensitivitiesRequestDialog {
    public static void showSettingsDialog(Context context) {

        StringBuffer infoBuffer = new StringBuffer();
        infoBuffer.append(context.getString(R.string.request_sensitivities_settings_message) + "\n");
        infoBuffer.append("-------------------------------------\n");
        infoBuffer.append("Model: " + Build.MODEL + "\n");
        infoBuffer.append("Device: " + Build.DEVICE + "\n");
        infoBuffer.append("Manufacturer: " + Build.MANUFACTURER + "\n");
        infoBuffer.append("-------------------------------------\n");
        infoBuffer.append("\n" + context.getString(R.string.request_sensitivities_settings_message_two));

        StringBuffer requestBuffer = new StringBuffer();

        requestBuffer.append("Sensitivities Request" + "\n\n");
        requestBuffer.append("-------------------------------------\n");
        requestBuffer.append("Model: " + Build.MODEL + "\n");
        requestBuffer.append("Device: " + Build.DEVICE + "\n");
        requestBuffer.append("Manufacturer: " + Build.MANUFACTURER + "\n");
        requestBuffer.append("-------------------------------------\n");
        //infoBuffer.append("\n\n");
        requestBuffer.append("\n" + "#SettingsRequest" + " " + "#" + Build.MANUFACTURER);

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.request_sensitivities_settings_title)
                .setMessage(infoBuffer)
                .setPositiveButton(R.string.request_sensitivities_settings_button, (dialog, which) -> {
                    new SendSensitivitiesRequestMessageToBot().execute(requestBuffer.toString());
                })
                .setNeutralButton(android.R.string.cancel, null)
                .show();
    }
}

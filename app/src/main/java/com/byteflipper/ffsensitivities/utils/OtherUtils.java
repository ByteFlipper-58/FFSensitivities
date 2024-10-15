package com.byteflipper.ffsensitivities.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewException;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OtherUtils {
    private Context context;

    public OtherUtils(Context context) {
        this.context = context;
    }

    public void copyTextToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied Text", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    public void reviewApp() {
        SharedPreferencesUtils.putInteger(context, "click_count", SharedPreferencesUtils.getInteger(context, "click_count", 0));
        int clickCount = SharedPreferencesUtils.getInteger(context, "click_count", 0);
        if (clickCount == 10)
            showReviewDialog();
        if (clickCount == 50)
            showReviewDialog();
        else
            SharedPreferencesUtils.putInteger(context, "click_count", clickCount + 1);
    }

    private void showReviewDialog() {
        ReviewManager manager = ReviewManagerFactory.create(context);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
            } else {
                @ReviewErrorCode int reviewErrorCode = ((ReviewException) task.getException()).getErrorCode();
                Log.d("TAG", "showReviewDialog: " + reviewErrorCode);
            }
        });
    }

    public String readFileFromAssets(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }
}
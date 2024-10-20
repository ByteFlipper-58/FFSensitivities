package com.byteflipper.ffsensitivities.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class InAppReviewHelper {

    private static InAppReviewHelper instance;
    private final ReviewManager reviewManager;
    private Activity currentActivity;
    private static final String PREFS_NAME = "InAppReviewPrefs";
    private static final String KEY_OPEN_COUNT = "open_count";
    private static final String KEY_REVIEW_SHOWN = "review_shown";
    private static final int REQUIRED_OPEN_COUNT = 3;

    private InAppReviewHelper(Application application) {
        reviewManager = ReviewManagerFactory.create(application);
    }

    public static InAppReviewHelper getInstance(Application application) {
        if (instance == null) {
            synchronized (InAppReviewHelper.class) {
                if (instance == null) {
                    instance = new InAppReviewHelper(application);
                }
            }
        }
        return instance;
    }

    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    public void onAppOpened() {
        SharedPreferences prefs = currentActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int openCount = prefs.getInt(KEY_OPEN_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_OPEN_COUNT, openCount).apply();

        // Проверка, достигнуто ли количество открытий
        if (openCount == REQUIRED_OPEN_COUNT && !prefs.getBoolean(KEY_REVIEW_SHOWN, false)) {
            requestReviewInfo(prefs);
        }
    }

    private void requestReviewInfo(SharedPreferences prefs) {
        Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                launchReviewFlow(reviewInfo, prefs);
            } else {
                Log.e("InAppReview", "Error requesting review flow: " + task.getException());
            }
        });
    }

    private void launchReviewFlow(ReviewInfo reviewInfo, SharedPreferences prefs) {
        if (currentActivity != null) {
            Task<Void> flow = reviewManager.launchReviewFlow(currentActivity, reviewInfo);
            flow.addOnCompleteListener(task -> {
                // Устанавливаем флаг, что отзыв был показан
                prefs.edit().putBoolean(KEY_REVIEW_SHOWN, true).apply();
                Log.d("InAppReview", "Review flow completed.");
            });
        } else {
            Log.e("InAppReview", "Current activity is null. Cannot launch review flow.");
        }
    }
}
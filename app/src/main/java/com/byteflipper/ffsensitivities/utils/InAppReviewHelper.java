package com.byteflipper.ffsensitivities.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class InAppReviewHelper {
    private static InAppReviewHelper instance;
    private final ReviewManager reviewManager;
    private ReviewInfo reviewInfo;

    private static final String LOG_TAG = "InAppReviewHelper";


    private InAppReviewHelper(Context context) {
        reviewManager = ReviewManagerFactory.create(context);
    }

    public static InAppReviewHelper getInstance(Context context) {
        if (instance == null) {
            instance = new InAppReviewHelper(context);
        }
        return instance;
    }


    public void requestReviewInfo() {
        Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInfo = task.getResult();
            } else {
                // There was some problem, continue regardless of the result.
                Log.e(LOG_TAG, "Ошибка запроса ReviewInfo", task.getException());
            }
        });
    }

    public void launchReviewFlow(Activity activity) {
        if (reviewInfo != null) {
            Task<Void> flow = reviewManager.launchReviewFlow(activity, reviewInfo);
            flow.addOnCompleteListener(task -> {
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Review flow launched successfully");
                } else {
                    Log.e(LOG_TAG, "Error launching review flow", task.getException());
                }

                reviewInfo = null;
            });
        }
    }}
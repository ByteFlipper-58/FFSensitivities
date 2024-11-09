package com.byteflipper.ffsensitivities.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class InAppReviewHelper {
    private static final String TAG = "InAppReviewHelper";
    private final ReviewManager reviewManager;

    // Constructor that takes an Activity as a parameter
    public InAppReviewHelper(Activity activity) {
        // Initialize the ReviewManager with the provided activity context
        reviewManager = ReviewManagerFactory.create(activity);
    }

    public void showRate(Activity activity) {
        Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();

                Task<Void> flow = reviewManager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(task1 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // There was some problem, continue regardless of the result.
                Log.e(TAG, "Error requesting review flow: " + task.getException());
                // Show native rate app dialog on error
                // showRateAppFallbackDialog(activity);

                redirectToPlayStore(activity);
            }
        });
    }

    public void redirectToPlayStore(Activity activity) {
        final String appPackageName = activity.getPackageName();
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException exception) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}

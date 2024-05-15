package com.byteflipper.ffsensitivities.utils;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

public class AppUpdateHelper {

    private static final String TAG = "APP UPDATE HELPER";
    private static final int REQUEST_CODE_IMMEDIATE = 1001;
    private static final int REQUEST_CODE_FLEXIBLE = 1002;
    private static final int PRIORITY_IMMEDIATE = 1;
    private static final int PRIORITY_FLEXIBLE = 2;

    private final Activity activity;
    private final AppUpdateManager appUpdateManager;
    private final UpdateListener updateListener;

    public AppUpdateHelper(Activity activity, UpdateListener updateListener) {
        this.activity = activity;
        this.updateListener = updateListener;
        appUpdateManager = AppUpdateManagerFactory.create(activity);
    }

    public void checkForAppUpdate() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            switch (appUpdateInfo.updateAvailability()) {
                case UpdateAvailability.UPDATE_AVAILABLE:
                    Log.d(TAG, "Доступно обновление.");
                    /*int updatePriority = getUpdatePriority();
                    if (updatePriority == PRIORITY_IMMEDIATE) {
                        startImmediateUpdate(appUpdateInfo);
                    } else if (updatePriority == PRIORITY_FLEXIBLE) {
                        startFlexibleUpdate(appUpdateInfo);
                        updateListener.onUpdateDownloadStarted();
                    }*/
                    updateListener.onUpdateAvailable();
                    break;
                case UpdateAvailability.UPDATE_NOT_AVAILABLE:
                    Log.d(TAG, "Обновление недоступно.");
                    updateListener.onUpdateNotAvailable();
                    break;
                default:
                    Log.d(TAG, "Проверка обновления.");
                    updateListener.onUpdateCheck();
                    break;
            }
        });
    }

    public void startImmediateUpdate(AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            try {
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        REQUEST_CODE_IMMEDIATE);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    public void startFlexibleUpdate(AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            try {
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        activity,
                        REQUEST_CODE_FLEXIBLE);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
            appUpdateManager.registerListener(installState -> {
                if (installState.installStatus() == InstallStatus.DOWNLOADING) {
                    // Отслеживание прогресса загрузки
                    int bytesDownloaded = (int) installState.bytesDownloaded();
                    int totalBytesToDownload = (int) installState.totalBytesToDownload();
                    float downloadProgress = bytesDownloaded / (float) totalBytesToDownload;
                    updateListener.onDownloadProgress(downloadProgress);
                    Log.d(TAG, "Ход загрузки: " + downloadProgress);
                } else if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                    updateListener.onUpdateDownloaded();
                }
            });
        }
    }

    public void startImmediateUpdateFromOutside() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(this::startImmediateUpdate);
    }

    public void startFlexibleUpdateFromOutside() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(this::startFlexibleUpdate);
    }

    public void completeUpdateFromOutside() {
        appUpdateManager.completeUpdate();
    }

    private int getUpdatePriority() {
        return PRIORITY_FLEXIBLE;
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_CODE_IMMEDIATE || requestCode == REQUEST_CODE_FLEXIBLE) {
            if (resultCode != Activity.RESULT_OK) {
                updateListener.onUpdateFailed();
                Log.e(TAG, "Ошибка обновления.");
            }
        }
    }

    public interface UpdateListener {
        void onUpdateCheck();
        void onUpdateNotAvailable();
        void onUpdateAvailable();
        void onUpdateDownloadStarted();
        void onDownloadProgress(float progress);
        void onUpdateDownloaded();
        void onUpdateFailed();
    }
}
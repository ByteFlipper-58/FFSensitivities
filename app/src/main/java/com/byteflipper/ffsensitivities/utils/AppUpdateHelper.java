package com.byteflipper.ffsensitivities.utils;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
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
        this.appUpdateManager = AppUpdateManagerFactory.create(activity);
    }

    public void checkForAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo != null) {
                handleUpdate(appUpdateInfo);
            } else {
                Log.e(TAG, "Ошибка получения информации о обновлении.");
                updateListener.onUpdateFailed();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Ошибка при проверке обновлений: " + e.getMessage());
            updateListener.onUpdateFailed();
        });
    }

    private void handleUpdate(AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            Log.d(TAG, "Доступно обновление.");
            updateListener.onUpdateAvailable();
            int updatePriority = getUpdatePriority();
            if (updatePriority == PRIORITY_IMMEDIATE) {
                startImmediateUpdate(appUpdateInfo);
            } else if (updatePriority == PRIORITY_FLEXIBLE) {
                startFlexibleUpdate(appUpdateInfo);
                updateListener.onUpdateDownloadStarted();
            }
        } else {
            handleNoUpdate(appUpdateInfo);
        }
    }

    private void handleNoUpdate(AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
            Log.d(TAG, "Обновление недоступно.");
            updateListener.onUpdateNotAvailable();
        } else {
            Log.d(TAG, "Проверка обновления.");
            updateListener.onUpdateCheck();
        }
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
                Log.e(TAG, "Ошибка запуска немедленного обновления: " + e.getMessage());
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
                Log.e(TAG, "Ошибка запуска гибкого обновления: " + e.getMessage());
            }
            registerInstallStateListener();
        }
    }

    private void registerInstallStateListener() {
        appUpdateManager.registerListener(installState -> {
            switch (installState.installStatus()) {
                case InstallStatus.DOWNLOADING:
                    trackDownloadProgress(installState);
                    break;
                case InstallStatus.DOWNLOADED:
                    updateListener.onUpdateDownloaded();
                    break;
            }
        });
    }

    private void trackDownloadProgress(InstallState installState) {
        int bytesDownloaded = (int) installState.bytesDownloaded();
        int totalBytesToDownload = (int) installState.totalBytesToDownload();
        float downloadProgress = (float) bytesDownloaded / totalBytesToDownload;
        updateListener.onDownloadProgress(downloadProgress);
        Log.d(TAG, "Ход загрузки: " + downloadProgress);
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_CODE_IMMEDIATE || requestCode == REQUEST_CODE_FLEXIBLE) {
            if (resultCode != Activity.RESULT_OK) {
                updateListener.onUpdateFailed();
                Log.e(TAG, "Ошибка обновления.");
            }
        }
    }

    private int getUpdatePriority() {
        return PRIORITY_FLEXIBLE;
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

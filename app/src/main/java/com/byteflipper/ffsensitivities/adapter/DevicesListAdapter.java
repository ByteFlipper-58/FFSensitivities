package com.byteflipper.ffsensitivities.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.model.SensitivityDataModel;
import com.byteflipper.ffsensitivities.ui.fragment.DevicesFragment;
import com.byteflipper.ffsensitivities.utils.NavigationOptionsUtil;

import java.util.List;

public class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.DeviceNameViewHolder> {
    private final NavController navController;
    private final DevicesFragment devicesFragment;
    private final List<SensitivityDataModel> models;

    public DevicesListAdapter(NavController navController, List<SensitivityDataModel> models, DevicesFragment devicesFragment) {
        this.navController = navController;
        this.models = models;
        this.devicesFragment = devicesFragment;
    }

    @NonNull
    @Override
    public DeviceNameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View deviceView = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_name_item, parent, false);
        return new DeviceNameViewHolder(deviceView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceNameViewHolder holder, int position) {
        SensitivityDataModel model = models.get(position);
        if (model != null) {
            holder.deviceName.setText(model.getManufacturerName() + " " + model.getDeviceName());
            holder.itemView.setOnClickListener(v -> showInterstitialAndNavigate(position));
        }
    }

    private void showInterstitialAndNavigate(int position) {
        devicesFragment.showInterstitialAd(() -> navigateToDeviceSettings(position));
    }

    private void navigateToDeviceSettings(int position) {
        SensitivityDataModel model = models.get(position);
        Bundle finalBundle = new Bundle();
        finalBundle.putString("device_model", model.getManufacturerName() + " " + model.getDeviceName());
        finalBundle.putFloat("review", model.getReview());
        finalBundle.putFloat("collimator", model.getCollimator());
        finalBundle.putFloat("x2_scope", model.getX2Scope());
        finalBundle.putFloat("x4_scope", model.getX4Scope());
        finalBundle.putFloat("sniper_scope", model.getSniperScope());
        finalBundle.putFloat("free_review", model.getFreeReview());
        finalBundle.putFloat("dpi", model.getDpi());
        finalBundle.putFloat("fire_button", model.getFireButton());
        finalBundle.putString("settings_source_url", model.getSettingsSourceUrl());
        navController.navigate(R.id.action_devicesFragment_to_sensitivitiesFragment, finalBundle, NavigationOptionsUtil.getNavOptions());
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class DeviceNameViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;

        public DeviceNameViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.categories);
        }
    }
}
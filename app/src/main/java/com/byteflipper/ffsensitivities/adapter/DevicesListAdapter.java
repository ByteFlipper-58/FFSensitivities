package com.byteflipper.ffsensitivities.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.model.SensitivityDataModel;
import com.byteflipper.ffsensitivities.ui.fragment.DevicesFragment;
import com.byteflipper.ffsensitivities.utils.NavigationOptionsUtil;

import java.util.List;

public class DevicesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Fragment fragment;
    private final List<SensitivityDataModel> models;
    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_BANNER = 1;

    public DevicesListAdapter(Fragment fragment, List<SensitivityDataModel> models) {
        this.fragment = fragment;
        this.models = models;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_BANNER:
                View bannerView = inflater.inflate(R.layout.banner_item, parent, false);
                return new BannerViewHolder(bannerView);
            case VIEW_TYPE_DEFAULT:
                View deviceView = inflater.inflate(R.layout.device_name_item, parent, false);
                return new DeviceNameViewHolder(deviceView);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof DeviceNameViewHolder) {
            DeviceNameViewHolder holder = (DeviceNameViewHolder) viewHolder;
            SensitivityDataModel model = models.get(position);
            if (model != null) {
                holder.deviceName.setText(model.getManufacturerName() + " " + model.getDeviceName());
                holder.itemView.setOnClickListener(v -> showInterstitialAndNavigate(position));
            }
        }
    }

    private void showInterstitialAndNavigate(int position) {
        if (fragment instanceof DevicesFragment) {
            DevicesFragment devicesFragment = (DevicesFragment) fragment;
            devicesFragment.showInterstitialAd(() -> navigateToDeviceSettings(position));
        }
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
        NavController navController = Navigation.findNavController(fragment.requireView());
        navController.navigate(R.id.action_devicesFragment_to_deviceSettingsFragment, finalBundle, NavigationOptionsUtil.getNavOptions());
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DEFAULT;
    }

    public static class DeviceNameViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;

        public DeviceNameViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.categories);
        }
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        FrameLayout bannerAdContainer;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerAdContainer = itemView.findViewById(R.id.banner_ad_container);
        }
    }
}
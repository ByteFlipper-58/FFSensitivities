package com.byteflipper.ffsensitivities.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.databinding.FragmentSensitivitiesBinding;
import com.byteflipper.ffsensitivities.utils.OtherUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SensitivitiesFragment extends Fragment {

    private FragmentSensitivitiesBinding binding;
    private float dpi = 0f;
    private Bundle finalBundle;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSensitivitiesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        finalBundle = getArguments();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity());
        FirebaseAnalytics.getInstance(requireActivity()).setAnalyticsCollectionEnabled(true);
        FirebaseAnalytics.getInstance(requireActivity()).setSessionTimeoutDuration(5000000);

        setupUI();
    }

    @SuppressLint("SetTextI18n")
    private void setupUI() {
        if (finalBundle != null) {
            float dpi = finalBundle.getFloat("dpi");
            float fireButton = finalBundle.getFloat("fire_button");

            if (dpi == 0) {
                binding.textViewDPI.setText(getString(R.string.fire_button) + ": " + (int) fireButton);
            } else {
                binding.textViewDPI.setText(getString(R.string.dpi) + ": " + (int) dpi + " | " + getString(R.string.fire_button) + ": " + (int) fireButton);
            }

            binding.reviewSlider.setValue(finalBundle.getFloat("review"));
            binding.collimatorSlider.setValue(finalBundle.getFloat("collimator"));
            binding.x2ScopeSlider.setValue(finalBundle.getFloat("x2_scope"));
            binding.x4ScopeSlider.setValue(finalBundle.getFloat("x4_scope"));
            //binding.sniperScope.setValue(finalBundle.getFloat("sniper_scope"));
            //binding.freeLookSlider.setValue(finalBundle.getFloat("free_review"));
            //binding.fireButtonSlider.setValue(finalBundle.getFloat("fire_button"));

            String sourceUrl = finalBundle.getString("settings_source_url");
            if (sourceUrl == null || sourceUrl.equals("null") || sourceUrl.isEmpty()) {
                binding.sourceButton.setVisibility(View.GONE);
            } else {
                binding.sourceButton.setOnClickListener(v -> openSourceUrl(sourceUrl));
            }

            binding.copyButton.setOnClickListener(v -> copySettingsToClipboard());

            binding.workBtn.setOnClickListener(v -> {
                logDeviceStatus(finalBundle.getString("device_model"), true);
                showToast(getString(R.string.thank_for_review));
            });

            binding.notWorkBtn.setOnClickListener(v -> {
                logDeviceStatus(finalBundle.getString("device_model"), false);
                showToast(getString(R.string.thank_for_review));
            });
        }
    }

    private void logDeviceStatus(String deviceModel, boolean isWorking) {
        Bundle bundle = new Bundle();
        bundle.putString("device_model", deviceModel);
        bundle.putString("device_status", isWorking ? "working" : "not_working");
        mFirebaseAnalytics.logEvent("device_status_event", bundle);
    }

    private void openSourceUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void copySettingsToClipboard() {
        try {
            float dpi = finalBundle.containsKey("dpi") ? finalBundle.getFloat("dpi") : 0;
            float review = finalBundle.containsKey("review") ? finalBundle.getFloat("review") : 0;
            float collimator = finalBundle.containsKey("collimator") ? finalBundle.getFloat("collimator") : 0;
            float x2Scope = finalBundle.containsKey("x2_scope") ? finalBundle.getFloat("x2_scope") : 0;
            float x4Scope = finalBundle.containsKey("x4_scope") ? finalBundle.getFloat("x4_scope") : 0;
            float sniperScope = finalBundle.containsKey("sniper_scope") ? finalBundle.getFloat("sniper_scope") : 0;
            float freeReview = finalBundle.containsKey("free_review") ? finalBundle.getFloat("free_review") : 0;
            String sourceUrl = finalBundle.getString("settings_source_url", "");

            StringBuilder settingsText = new StringBuilder();
            settingsText.append(getString(R.string.dpi)).append(": ").append((int) dpi).append("\n")
                    .append(getString(R.string.review)).append(": ").append((int) review).append("\n")
                    .append(getString(R.string.collimator)).append(": ").append((int) collimator).append("\n")
                    .append(getString(R.string.x2_scope)).append(": ").append((int) x2Scope).append("\n")
                    .append(getString(R.string.x4_scope)).append(": ").append((int) x4Scope).append("\n")
                    .append(getString(R.string.sniper_scope)).append(": ").append((int) sniperScope).append("\n")
                    .append(getString(R.string.free_review)).append(": ").append((int) freeReview).append("\n")
                    .append(getString(R.string.source)).append(" ").append(sourceUrl);

            new OtherUtils(requireActivity()).copyTextToClipboard(settingsText.toString());
            showToast(getString(R.string.copied));
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error: " + e.getMessage());
        }

    }

    private void showToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
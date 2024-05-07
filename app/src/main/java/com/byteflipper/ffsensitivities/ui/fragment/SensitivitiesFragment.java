package com.byteflipper.ffsensitivities.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
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

public class SensitivitiesFragment extends Fragment {

    private FragmentSensitivitiesBinding binding;
    private float dpi = 0f;
    private Bundle finalBundle;

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
        setupUI();
    }

    @SuppressLint("SetTextI18n")
    private void setupUI() {
        if (finalBundle != null) {
            dpi = finalBundle.getFloat("dpi");
            if (dpi == 0) {
                binding.textViewDPI.setVisibility(View.GONE);
            } else {
                binding.textViewDPI.setText(getString(R.string.dpi) + ":" + " " + (int) dpi);
            }

            binding.reviewSlider.setValue(finalBundle.getFloat("review"));
            binding.collimatorSlider.setValue(finalBundle.getFloat("collimator"));
            binding.x2ScopeSlider.setValue(finalBundle.getFloat("x2_scope"));
            binding.x4ScopeSlider.setValue(finalBundle.getFloat("x4_scope"));
            binding.sniperScope.setValue(finalBundle.getFloat("sniper_scope"));
            binding.freeLookSlider.setValue(finalBundle.getFloat("free_review"));
            binding.fireButtonSlider.setValue(finalBundle.getFloat("fire_button"));

            String sourceUrl = finalBundle.getString("settings_source_url");
            if (sourceUrl == null || sourceUrl.equals("null") || sourceUrl.isEmpty()) {
                binding.sourceButton.setVisibility(View.GONE);
            } else {
                binding.sourceButton.setOnClickListener(v -> openSourceUrl(sourceUrl));
            }

            binding.copyButton.setOnClickListener(v -> copySettingsToClipboard());
        }
    }

    private void openSourceUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void copySettingsToClipboard() {
        try {
            String settingsText = getString(R.string.dpi) + ":" + " " + (int) dpi + "\n" +
                    getString(R.string.review) + ":" + " " + (int) finalBundle.getFloat("review") + "\n" +
                    getString(R.string.collimator) + ":" + " " + (int) finalBundle.getFloat("collimator") + "\n" +
                    getString(R.string.x2_scope) + ":" + " " + (int) finalBundle.getFloat("x2_scope") + "\n" +
                    getString(R.string.x4_scope) + ":" + " " + (int) finalBundle.getFloat("x4_scope") + "\n" +
                    getString(R.string.sniper_scope) + ":" + " " + (int) finalBundle.getFloat("sniper_scope") + "\n" +
                    getString(R.string.free_review) + ":" + " " + (int) finalBundle.getFloat("free_review") + "\n" +
                    getString(R.string.fire_button) + ":" + " " + (int) finalBundle.getFloat("fire_button") + "\n" +
                    getString(R.string.source) + " " + finalBundle.getString("settings_source_url");
            new OtherUtils(requireActivity()).copyTextToClipboard(settingsText);
            showToast("Copied");
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
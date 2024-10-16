package com.byteflipper.ffsensitivities.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.manager.LanguageManager;
import com.byteflipper.ffsensitivities.ui.MainActivity;
import com.google.android.material.color.DynamicColors;
import com.byteflipper.ffsensitivities.databinding.FragmentSettingsBinding;
import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    private static final String TAG = "TEST";

    private String[] languages = {"English", "Belarusian", "German (Austria)", "German (Belgium)", "French", "Polish", "Russian", "Turkish", "Ukrainian"};

    private String[] languageCodes = {"en", "be", "de-rAT", "de-rBE", "fr", "pl", "ru", "tr", "uk"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.appThemeRadioGroup.check(SharedPreferencesUtils.getInteger(requireContext(), "checkedButton", R.id.setFollowSystemTheme));
        binding.dynamicColorsSwitch.setEnabled(DynamicColors.isDynamicColorAvailable());
        int[] setNightModeDescription = {R.string.system_theme_description, R.string.light_theme_description, R.string.night_theme_description};
        binding.themeDescription.setText(setNightModeDescription[SharedPreferencesUtils.getInteger(requireContext(), "nightMode", 0)]);

        binding.appThemeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.setFollowSystemTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    SharedPreferencesUtils.putInteger(requireContext(), "checkedButton", R.id.setFollowSystemTheme);
                    SharedPreferencesUtils.putInteger(requireContext(), "nightMode", 0);
                    restartApp();
                    break;
                case R.id.setLightTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    SharedPreferencesUtils.putInteger(requireContext(), "checkedButton", R.id.setLightTheme);
                    SharedPreferencesUtils.putInteger(requireContext(), "nightMode", 1);
                    restartApp();
                    break;
                case R.id.setNightTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    SharedPreferencesUtils.putInteger(requireContext(), "checkedButton", R.id.setNightTheme);
                    SharedPreferencesUtils.putInteger(requireContext(), "nightMode", 2);
                    restartApp();
                    break;
            }
        });

        LanguageManager languageManager = new LanguageManager();
        binding.setLanguageButton.setOnClickListener(v -> {
            LanguageManager.showLanguageDialog(requireActivity());
        });

        binding.setLanguageButton.setSubtitleText(LanguageManager.getCurrentLanguage(requireActivity()));

        binding.dynamicColorsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            restartApp();
            Log.d("CustomSwitchView", "Switch checked: " + isChecked);
        });

        if (SharedPreferencesUtils.getBoolean(requireContext(), "dev_mode"))
            binding.devModeSettings.setVisibility(View.VISIBLE);
        else
            binding.devModeSettings.setVisibility(View.GONE);
    }

    void restartApp() {
        requireActivity().finish();
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.putExtra("openSettingsFragment", true);
        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
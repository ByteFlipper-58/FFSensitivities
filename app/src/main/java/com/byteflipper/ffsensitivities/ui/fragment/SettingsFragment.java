package com.byteflipper.ffsensitivities.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.callback.CallbackManager;
import com.byteflipper.ffsensitivities.manager.LanguageManager;
import com.google.android.material.color.DynamicColors;
import com.byteflipper.ffsensitivities.databinding.FragmentSettingsBinding;
import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    private static final String TAG = "TEST";

    private String[] languages = {
            "English",
            "Belarusian",
            "German (Austria)",
            "German (Belgium)",
            "French",
            "Polish",
            "Russian",
            "Turkish",
            "Ukrainian"
    };

    private String[] languageCodes = {
            "en",
            "be",
            "de-rAT",
            "de-rBE",
            "fr",
            "pl",
            "ru",
            "tr",
            "uk"
    };

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
        binding.dynamicColorsSwitch.setChecked(SharedPreferencesUtils.getBoolean(requireContext(), "useDynamicColors"));
        int[] setNightModeDescription = {R.string.system_theme_description, R.string.light_theme_description, R.string.night_theme_description};
        binding.themeDescription.setText(setNightModeDescription[SharedPreferencesUtils.getInteger(requireContext(), "nightMode", 0)]);

        binding.appThemeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.setFollowSystemTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    SharedPreferencesUtils.writeInteger(requireContext(), "checkedButton", R.id.setFollowSystemTheme);
                    SharedPreferencesUtils.writeInteger(requireContext(), "nightMode", 0);
                    requireActivity().recreate();
                    break;
                case R.id.setLightTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    SharedPreferencesUtils.writeInteger(requireContext(), "checkedButton", R.id.setLightTheme);
                    SharedPreferencesUtils.writeInteger(requireContext(), "nightMode", 1);
                    requireActivity().recreate();
                    break;
                case R.id.setNightTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    SharedPreferencesUtils.writeInteger(requireContext(), "checkedButton", R.id.setNightTheme);
                    SharedPreferencesUtils.writeInteger(requireContext(), "nightMode", 2);
                    requireActivity().recreate();
                    break;
            }
            CallbackManager.invokeCallback(requireActivity());
        });

        LanguageManager languageManager = new LanguageManager();
        binding.setLanguageButton.setOnClickListener(v -> {
            LanguageManager.showLanguageDialog(requireActivity());
        });

        binding.setLanguageButton.setSubtitleText(LanguageManager.getCurrentLanguage(requireActivity()));

        binding.dynamicColorsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.writeBoolean(requireContext(), "useDynamicColors", isChecked);
            requireActivity().recreate();
            CallbackManager.invokeCallback(requireActivity());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
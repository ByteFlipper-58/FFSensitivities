package com.byteflipper.ffsensitivities.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.byteflipper.ffsensitivities.BuildConfig;
import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.databinding.FragmentAboutBinding;
import com.byteflipper.ffsensitivities.utils.BugReportHelper;
import com.byteflipper.ffsensitivities.utils.ChromeCustomTabUtil;
import com.byteflipper.ffsensitivities.utils.OtherUtils;

public class AboutFragment extends Fragment {
    private FragmentAboutBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.appVersionBtn.setText(getString(R.string.version) + ": " + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
        binding.translateAppBtn.setOnClickListener(view1 -> {
            new ChromeCustomTabUtil.Builder(requireContext(), getString(R.string.crowdin)).open();
        });
        //binding.donateBtn.setOnClickListener(v -> new ChromeCustomTabUtil().OpenCustomTab(getActivity(), "https://www.donationalerts.com/r/ibremminer837", R.color.md_theme_light_onSecondary));
        //binding.sourceCodeBtn.setOnClickListener(v -> new ChromeCustomTabUtil().OpenCustomTab(getActivity(), getString(R.string.source_code_url), R.color.md_theme_light_onSecondary));
        //binding.ibragimBtn.setOnClickListener(v -> new ChromeCustomTabUtil().OpenCustomTab(getActivity(), getString(R.string.ibragim_url), R.color.md_theme_light_onSecondary));
        binding.mailBtn.setOnClickListener(v -> BugReportHelper.sendEmail(getActivity()));
        binding.rateBtn.setOnClickListener(v -> new OtherUtils(getActivity()).reviewAppInGooglePlay());
        //binding.vkGroupBtn.setOnClickListener(v -> new ChromeCustomTabUtil().OpenCustomTab(getActivity(), getString(R.string.JVMFrog_Squad), R.color.md_theme_light_onSecondary));
        //binding.telegramBtn.setOnClickListener(v -> new ChromeCustomTabUtil().OpenCustomTab(getActivity(), "https://t.me/freefiresettingsapp", R.color.md_theme_light_onSecondary));
        binding.otherAppsBtn.setOnClickListener(view1 -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("rustore://apps.rustore.ru/app/" + BuildConfig.APPLICATION_ID)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
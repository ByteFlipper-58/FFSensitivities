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
import com.byteflipper.ffsensitivities.utils.ChromeCustomTabUtil;
import com.byteflipper.ffsensitivities.utils.FeedbackHelper;
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

        binding.bugReportBtn.setOnClickListener(v -> {
            FeedbackHelper.sendEmail(getActivity());
        });

        binding.rateReviewBtn.setOnClickListener(v -> {
            new OtherUtils(getActivity()).reviewAppInGooglePlay();
        });

        binding.otherAppsBtn.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
            } catch (android.content.ActivityNotFoundException anfe) {
                //open play store
            }
        });

        binding.websiteBtn.setOnClickListener(v -> {
            new ChromeCustomTabUtil.Builder(requireContext(), getString(R.string.website_url)).open();
        });

        binding.vkBtn.setOnClickListener(v -> {
            new ChromeCustomTabUtil.Builder(requireContext(), getString(R.string.vk_url)).open();
        });

        binding.telegramBtn.setOnClickListener(v -> {
            new ChromeCustomTabUtil.Builder(requireContext(), getString(R.string.telegram_url)).open();
        });

        binding.githubBtn.setOnClickListener(v -> {
            new ChromeCustomTabUtil.Builder(requireContext(), getString(R.string.github_url)).open();
        });

        binding.sourceCode.setOnClickListener(v -> {
            new ChromeCustomTabUtil.Builder(requireContext(), getString(R.string.source_code_url)).open();
        });

        binding.version.setSubtitleText(BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
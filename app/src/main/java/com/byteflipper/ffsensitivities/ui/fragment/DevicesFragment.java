package com.byteflipper.ffsensitivities.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;

import com.byteflipper.ffsensitivities.MyApp;
import com.byteflipper.ffsensitivities.interfaces.IScrollHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.adapter.DevicesListAdapter;
import com.byteflipper.ffsensitivities.databinding.FragmentDevicesBinding;
import com.byteflipper.ffsensitivities.manager.SensitivitiesManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class DevicesFragment extends Fragment implements IScrollHelper {

    private FragmentDevicesBinding binding;
    private SensitivitiesManager manager;
    private LinearProgressIndicator indicator;
    private InterstitialAd mInterstitialAd;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);
        setupActionBar();
        setupViews();
        loadInterstitialAd();
        return binding.getRoot();
    }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViews() {
        indicator = requireActivity().findViewById(R.id.progressIndicator);
        manager = new SensitivitiesManager();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getContext(), "ca-app-pub-4346225518624754/3444991490", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    public void showInterstitialAd(Runnable onAdDismissed) {
        MyApp app = (MyApp) requireActivity().getApplication();
        if (app.isAdShowing()) {
            onAdDismissed.run();
            return;
        }
        app.setAdShowing(true);  // Устанавливаем флаг перед показом рекламы

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null;
                    loadInterstitialAd();
                    app.setAdShowing(false);  // Сбрасываем флаг после показа рекламы
                    onAdDismissed.run();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    mInterstitialAd = null;
                    loadInterstitialAd();
                    app.setAdShowing(false);  // Сбрасываем флаг после показа рекламы
                    onAdDismissed.run();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    mInterstitialAd = null;
                }
            });
            mInterstitialAd.show(requireActivity());
        } else {
            app.setAdShowing(false);  // Сбрасываем флаг, если реклама не готова
            onAdDismissed.run();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDataObserver();
        setupAdapterData();
    }

    private void setupDataObserver() {
        manager.isRequestFinished().observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                showLoadingState();
            } else {
                hideLoadingState();
                setupRecyclerView();
            }
        });
    }

    private void setupAdapterData() {
        Bundle finalBundle = new Bundle();
        finalBundle.putAll(getArguments());
        manager.updateAdapterData(requireActivity(), finalBundle.getString("model"));
    }

    private void showLoadingState() {
        indicator.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.recview.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        indicator.setVisibility(View.GONE);
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
        binding.recview.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        binding.recview.setLayoutManager(new GridLayoutManager(requireActivity(), 1));
        binding.recview.setAdapter(new DevicesListAdapter(this, manager.getSensitivitiesSet()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void scrollToTop() {
        binding.recview.smoothScrollToPosition(0);
    }
}
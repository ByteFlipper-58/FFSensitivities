package com.byteflipper.ffsensitivities.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.byteflipper.ffsensitivities.MyApplication;
import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.interfaces.IScrollHelper;
import com.byteflipper.ffsensitivities.interfaces.ProgressIndicatorListener;

import com.byteflipper.ffsensitivities.adapter.DevicesListAdapter;
import com.byteflipper.ffsensitivities.databinding.FragmentDevicesBinding;
import com.byteflipper.ffsensitivities.manager.SensitivitiesManager;
import com.byteflipper.ffsensitivities.utils.NavigationOptionsUtil;
import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;

public class DevicesFragment extends Fragment implements IScrollHelper {

    private FragmentDevicesBinding binding;
    private SensitivitiesManager manager;
    private ProgressIndicatorListener progressIndicatorListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProgressIndicatorListener) {
            progressIndicatorListener = (ProgressIndicatorListener) context;
        } else {
            throw new RuntimeException(context + " must implement ProgressIndicatorListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);
        setupActionBar();
        setupViews();
        return binding.getRoot();
    }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViews() {
        manager = new SensitivitiesManager();
    }

    public void showInterstitialAd(Runnable onAdDismissed) {
        if (MyApplication.isAdReady()) {
            InterstitialAd interstitialAd = MyApplication.getInterstitialAd();
            Log.d("DevicesFragment", "Interstitial ad is ready, showing...");

            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d("DevicesFragment", "Interstitial ad dismissed.");
                    // Обязательно перезагружаем рекламу после показа
                    MyApplication.reloadAd(getContext());
                    onAdDismissed.run();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.d("DevicesFragment", "Failed to show interstitial ad: " + adError.getMessage());
                    onAdDismissed.run();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d("DevicesFragment", "Interstitial ad shown.");
                    MyApplication.mInterstitialAd = null;
                }
            });

            // Показываем рекламу
            interstitialAd.show(requireActivity());
        } else {
            Log.d("DevicesFragment", "Interstitial ad is not ready yet.");
            // Если реклама не готова, просто выполняем переданный код
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

            if (SharedPreferencesUtils.getBoolean(requireActivity(), "enableShimmerLayoutInDevices")) {
                showLoadingState();
                progressIndicatorListener.hideProgress();
            }

            if (SharedPreferencesUtils.getBoolean(requireActivity(), "enableShimmerLayoutInDevices"))
                binding.shimmerLayout.setOnClickListener(
                        v -> {
                            Bundle finalBundle = new Bundle();
                            finalBundle.putString("device_model", "Samsung S24");
                            finalBundle.putFloat("review", 128);
                            finalBundle.putFloat("collimator", 96);
                            finalBundle.putFloat("x2_scope", 86);
                            finalBundle.putFloat("x4_scope", 86);
                            finalBundle.putFloat("sniper_scope", 42);
                            finalBundle.putFloat("free_review", 51);
                            finalBundle.putFloat("dpi", 386);
                            finalBundle.putFloat("fire_button", 42);
                            finalBundle.putString("settings_source_url", null);
                            NavController navController = Navigation.findNavController(requireView());
                            navController.navigate(R.id.action_devicesFragment_to_deviceSettingsFragment, finalBundle, NavigationOptionsUtil.getNavOptions());
                        }
                );

            if (SharedPreferencesUtils.getBoolean(requireActivity(), "enableShimmerEffectInDevices"))
                binding.shimmerLayout.startShimmer();
            else
                binding.shimmerLayout.stopShimmer();
        });
    }

    private void setupAdapterData() {
        Bundle finalBundle = new Bundle();
        finalBundle.putAll(getArguments());
        manager.updateAdapterData(requireActivity(), finalBundle.getString("model"));
    }

    private void showLoadingState() {
        progressIndicatorListener.showProgress();
        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.recview.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        progressIndicatorListener.hideProgress();
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
package com.byteflipper.ffsensitivities.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.byteflipper.ffsensitivities.interfaces.ProgressIndicatorListener;
import com.byteflipper.ffsensitivities.settingsrequest.SensitivitiesRequestDialog;
import com.byteflipper.ffsensitivities.databinding.FragmentManufacturersBinding;
import com.byteflipper.ffsensitivities.utils.ChromeCustomTabUtil;
import com.byteflipper.ffsensitivities.utils.NavigationOptionsUtil;
import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.adapter.ManufacturersListAdapter;
import com.byteflipper.ffsensitivities.manager.ManufacturersManager;

import java.util.Locale;

public class ManufacturersFragment extends Fragment {
    private FragmentManufacturersBinding binding;
    private ManufacturersManager manager;
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
        binding = FragmentManufacturersBinding.inflate(inflater, container, false);
        manager = ManufacturersManager.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        manager.isRequestFinished().observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                progressIndicatorListener.showProgress();
                binding.shimmerLayout.startShimmer();
                binding.shimmerLayout.setVisibility(View.VISIBLE);
                binding.recview.setVisibility(View.GONE);
            } else {
                progressIndicatorListener.hideProgress();
                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.setVisibility(View.GONE);
                binding.recview.setVisibility(View.VISIBLE);
                binding.recview.setLayoutManager(new GridLayoutManager(requireActivity(), 2));
                binding.recview.setAdapter(new ManufacturersListAdapter(this, manager.getManufacturersSet()));
            }

            if (SharedPreferencesUtils.getBoolean(requireActivity(), "enableShimmerLayoutInManufacturers")) {
                progressIndicatorListener.hideProgress();
                binding.shimmerLayout.setVisibility(View.VISIBLE);
                binding.recview.setVisibility(View.GONE);
            }
        });

        binding.subscribeBtn.setOnClickListener(v -> {
            new ChromeCustomTabUtil.Builder(requireContext(), "https://t.me/byteflipper").open();
        });

        if (SharedPreferencesUtils.getBoolean(requireActivity(), "sensitivities_is_requested")) {
            binding.setUserNameBtn.setEnabled(false);
            binding.welcomeAndUserName.setText(getString(R.string.request_sensitivities_settings_success));
        }


        if (SharedPreferencesUtils.getBoolean(requireActivity(), "enableShimmerEffectInManufacturers"))
            binding.shimmerLayout.startShimmer();
        else
            binding.shimmerLayout.stopShimmer();

        if (SharedPreferencesUtils.getBoolean(requireActivity(), "enableShimmerLayoutInManufacturers"))
            binding.shimmerLayout.setOnClickListener(
                    v -> {
                        Bundle finalBundle = new Bundle();
                        finalBundle.putString("model", "samsung");
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_manufacturerFragment2_to_devicesFragment, finalBundle, NavigationOptionsUtil.getNavOptions());
                    }
            );

        if (SharedPreferencesUtils.getBoolean(requireActivity(), "enableShimmerEffectInManufacturers"))
            binding.shimmerLayout.startShimmer();
        else
            binding.shimmerLayout.stopShimmer();

        binding.setUserNameBtn.setOnClickListener(v -> {
            SensitivitiesRequestDialog.showSettingsDialog(requireActivity());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.byteflipper.ffsensitivities.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.byteflipper.ffsensitivities.settingsrequest.SensitivitiesRequestDialog;
import com.byteflipper.ffsensitivities.databinding.FragmentManufacturersBinding;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.adapter.ManufacturersListAdapter;
import com.byteflipper.ffsensitivities.manager.ManufacturersManager;

public class ManufacturersFragment extends Fragment {
    private FragmentManufacturersBinding binding;
    private ManufacturersManager manager;
    private LinearProgressIndicator indicator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManufacturersBinding.inflate(inflater, container, false);
        indicator = requireActivity().findViewById(R.id.progressIndicator);
        manager = ManufacturersManager.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        manager.isRequestFinished().observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                //indicator.setVisibility(View.VISIBLE);
                binding.shimmerLayout.startShimmer();
                binding.shimmerLayout.setVisibility(View.VISIBLE);
                binding.recview.setVisibility(View.GONE);
            } else {
                //indicator.setVisibility(View.GONE);
                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.setVisibility(View.GONE);
                binding.recview.setVisibility(View.VISIBLE);
                binding.recview.setLayoutManager(new GridLayoutManager(requireActivity(), 2));
                binding.recview.setAdapter(new ManufacturersListAdapter(this, manager.getManufacturersSet()));
            }
        });

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
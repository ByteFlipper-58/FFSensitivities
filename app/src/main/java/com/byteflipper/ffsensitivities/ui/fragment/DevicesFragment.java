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

import com.byteflipper.ffsensitivities.interfaces.IScrollHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.adapter.DevicesListAdapter;
import com.byteflipper.ffsensitivities.databinding.FragmentDevicesBinding;
import com.byteflipper.ffsensitivities.manager.SensitivitiesManager;

public class DevicesFragment extends Fragment implements IScrollHelper {

    private FragmentDevicesBinding binding;
    private SensitivitiesManager manager;
    private LinearProgressIndicator indicator;

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
        indicator = requireActivity().findViewById(R.id.progressIndicator);
        manager = new SensitivitiesManager();
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
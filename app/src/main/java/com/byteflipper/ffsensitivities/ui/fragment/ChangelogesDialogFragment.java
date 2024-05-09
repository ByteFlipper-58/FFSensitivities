package com.byteflipper.ffsensitivities.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.byteflipper.ffsensitivities.databinding.FragmentChangelogesDialogListDialogBinding;
import com.byteflipper.ffsensitivities.databinding.FragmentChangelogesDialogListDialogItemBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textview.MaterialTextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ChangelogesDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_ITEM_COUNT = "item_count";
    private FragmentChangelogesDialogListDialogBinding binding;

    // TODO: Customize parameters
    public static ChangelogesDialogFragment newInstance(int itemCount) {
        final ChangelogesDialogFragment fragment = new ChangelogesDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_ITEM_COUNT, itemCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentChangelogesDialogListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Устанавливаем LayoutManager и адаптер
        binding.list.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.list.setAdapter(new ItemAdapter(getArguments().getInt(ARG_ITEM_COUNT)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        final MaterialTextView text;

        ViewHolder(FragmentChangelogesDialogListDialogItemBinding binding) {
            super(binding.getRoot());
            text = binding.changelog;
        }

    }

    private static class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;

        ItemAdapter(int itemCount) {
            mItemCount = itemCount;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new ViewHolder(FragmentChangelogesDialogListDialogItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(String.valueOf(position));
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

    }
}
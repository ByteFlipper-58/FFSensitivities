package com.byteflipper.ffsensitivities.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.model.ManufacturersDataModel;
import com.byteflipper.ffsensitivities.utils.NavigationOptionsUtil;

public class ManufacturersListAdapter extends RecyclerView.Adapter<ManufacturersListAdapter.ManufacturersHolder> {
    private final Fragment fragment;
    private final List<ManufacturersDataModel> models;

    public ManufacturersListAdapter(Fragment fragment, List<ManufacturersDataModel> models) {
        this.fragment = fragment;
        this.models = models;
    }

    @Override
    public void onBindViewHolder(@NonNull ManufacturersHolder holder, int position) {
        if (models.get(position).getShowInProductionApp()) {
            holder.manufacturerName.setText(models.get(position).getName());
        } else {
            holder.itemView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle finalBundle = new Bundle();
            finalBundle.putString("model", models.get(position).getModel().toLowerCase(Locale.ROOT));
            NavController navController = Navigation.findNavController(fragment.requireView());
            navController.navigate(R.id.action_manufacturerFragment2_to_devicesFragment, finalBundle, NavigationOptionsUtil.getNavOptions());
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @NonNull
    @Override
    public ManufacturersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_name_item, parent, false);
        return new ManufacturersHolder(view);
    }

    public static class ManufacturersHolder extends RecyclerView.ViewHolder {
        TextView manufacturerName;
        public ManufacturersHolder(@NonNull View itemView) {
            super(itemView);
            manufacturerName = itemView.findViewById(R.id.categories);
        }
    }
}
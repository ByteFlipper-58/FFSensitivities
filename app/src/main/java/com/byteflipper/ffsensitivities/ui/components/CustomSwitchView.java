package com.byteflipper.ffsensitivities.ui.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.byteflipper.ffsensitivities.R;
import com.byteflipper.ffsensitivities.databinding.CustomSwitchViewBinding;

public class CustomSwitchView extends RelativeLayout {
    private CustomSwitchViewBinding binding;
    private String switchSubtitleOn;
    private String switchSubtitleOff;
    private String key;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public CustomSwitchView(Context context) {
        this(context, null);
    }

    public CustomSwitchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        binding = CustomSwitchViewBinding.inflate(LayoutInflater.from(context), this);
        loadAttributes(context, attrs);
        setupSwitch(context);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSwitchView);
        String switchTitle = a.getString(R.styleable.CustomSwitchView_switchTitle);
        switchSubtitleOn = a.getString(R.styleable.CustomSwitchView_switchSubtitleOn);
        switchSubtitleOff = a.getString(R.styleable.CustomSwitchView_switchSubtitleOff);
        key = a.getString(R.styleable.CustomSwitchView_key);
        boolean switchChecked = a.getBoolean(R.styleable.CustomSwitchView_defaultValue, false);
        a.recycle();

        if (key != null) {
            SharedPreferences prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE);
            switchChecked = prefs.getBoolean(key, switchChecked);
        }

        binding.switchViewTitle.setText(switchTitle);
        setCheckedState(switchChecked);
    }

    private void setupSwitch(Context context) {
        binding.switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setCheckedState(isChecked);
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
            }

            // Сохраняем состояние в SharedPreferences
            if (key != null) {
                SharedPreferences prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(key, isChecked);
                editor.apply();
            }
        });
    }

    private void setCheckedState(boolean isChecked) {
        binding.switchView.setChecked(isChecked);
        binding.switchViewSubtitle.setText(isChecked ? switchSubtitleOn : switchSubtitleOff);
    }

    public void setChecked(boolean checked) {
        setCheckedState(checked);
        if (key != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("app_config", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(key, checked);
            editor.apply();
        }
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }
}
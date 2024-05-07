package com.byteflipper.ffsensitivities.ui.components;

import android.content.Context;
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
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public CustomSwitchView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        binding = CustomSwitchViewBinding.inflate(LayoutInflater.from(context), this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSwitchView);
        String switchTitle = a.getString(R.styleable.CustomSwitchView_switchTitle);
        switchSubtitleOn = a.getString(R.styleable.CustomSwitchView_switchSubtitleOn);
        switchSubtitleOff = a.getString(R.styleable.CustomSwitchView_switchSubtitleOff);
        boolean switchChecked = a.getBoolean(R.styleable.CustomSwitchView_switchValue, false);
        a.recycle();

        binding.switchViewTitle.setText(switchTitle);
        binding.switchViewSubtitle.setText(switchChecked ? switchSubtitleOn : switchSubtitleOff);
        binding.switchView.setChecked(switchChecked);

        binding.switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.switchViewSubtitle.setText(isChecked ? switchSubtitleOn : switchSubtitleOff);
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
            }
        });
    }

    public void setChecked(boolean checked) {
        binding.switchView.setChecked(checked);
        binding.switchViewSubtitle.setText(checked ? switchSubtitleOn : switchSubtitleOff);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }
}
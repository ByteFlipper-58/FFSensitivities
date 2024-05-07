package com.byteflipper.ffsensitivities.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.byteflipper.ffsensitivities.R;
import com.google.android.material.textview.MaterialTextView;

public class CustomItemView extends RelativeLayout {
    private ImageView mImageView;
    private MaterialTextView mTitleTextView;
    private MaterialTextView mSubtitleTextView;

    public CustomItemView(Context context) {
        super(context);
        init(null);
    }

    public CustomItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // Inflate layout
        inflate(getContext(), R.layout.item_list, this);

        // Find views
        mImageView = findViewById(R.id.list_view_icon);
        mTitleTextView = findViewById(R.id.list_view_title);
        mSubtitleTextView = findViewById(R.id.list_view_subtitle);

        if (attrs != null) {
            // Get attributes
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomItemView);
            String titleText = a.getString(R.styleable.CustomItemView_listTitle);
            String subtitleText = a.getString(R.styleable.CustomItemView_listSubtitle);
            int iconResId = a.getResourceId(R.styleable.CustomItemView_listIcon, 0);
            a.recycle();

            // Set values
            mImageView.setImageResource(iconResId);
            mTitleTextView.setText(titleText);
            mSubtitleTextView.setText(subtitleText);
        }
    }

    // Методы для установки изображения, заголовка и подзаголовка
    public void setImageDrawable(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }

    public void setTitleText(String text) {
        mTitleTextView.setText(text);
    }

    public void setSubtitleText(String text) {
        mSubtitleTextView.setText(text);
    }
}
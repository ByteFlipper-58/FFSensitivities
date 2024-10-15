package com.byteflipper.ffsensitivities.listeners;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.byteflipper.ffsensitivities.utils.SharedPreferencesUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CustomTouchListener implements View.OnTouchListener {

    private static final int MAX_CLICK_DURATION = 500; // Максимальная продолжительность для определения быстрого клика
    private static final int REQUIRED_CLICKS = 6; // Необходимое количество кликов для активации функции

    private int clickCount = 0; // Счётчик кликов
    private long startClickTime = 0; // Время начала клика

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Сохраняем время нажатия
                startClickTime = System.currentTimeMillis();
                v.setPressed(true); // Эффект фокуса при нажатии
                break;

            case MotionEvent.ACTION_UP:
                long clickDuration = System.currentTimeMillis() - startClickTime;

                // Если клик был быстрым
                if (clickDuration < MAX_CLICK_DURATION) {
                    clickCount++; // Увеличиваем счётчик кликов
                    handleClick(v);
                }

                // Проверка на долгое нажатие только при 6 кликах
                if (clickDuration >= MAX_CLICK_DURATION && clickCount >= REQUIRED_CLICKS) {
                    onSeventhLongPress(v);
                }

                // Выполнить стандартный клик
                v.performClick();
                v.setPressed(false); // Убираем эффект фокуса

                // Сбрасываем счётчик, если он превышает 6
                if (clickCount > REQUIRED_CLICKS) {
                    resetClickCount();
                }
                break;
        }
        return true; // Возвращаем true, чтобы сигнализировать о том, что событие обработано
    }

    private void handleClick(View v) {
        if (clickCount < REQUIRED_CLICKS) {
            //Toast.makeText(v.getContext(), "Quick click: " + clickCount, Toast.LENGTH_SHORT).show();
        } else if (clickCount == REQUIRED_CLICKS) {
            //Toast.makeText(v.getContext(), "6 clicks!", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetClickCount() {
        clickCount = 0;
    }

    private void onSeventhLongPress(View v) {
        new MaterialAlertDialogBuilder(v.getContext())
                .setTitle("Enable Dev Features")
                .setMessage("Are you sure you want to enable developer features?")
                .setPositiveButton("Active", (dialog, which) -> {
                    SharedPreferencesUtils.putBoolean(v.getContext(), "dev_mode", true);
                    Toast.makeText(v.getContext(), "Developer features enabled", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Deactivate", (dialog, which) -> {
                    SharedPreferencesUtils.putBoolean(v.getContext(), "dev_mode", false);
                    Toast.makeText(v.getContext(), "Developer features deactivated", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }
}

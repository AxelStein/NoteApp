package com.axel_stein.noteapp.utils;

import android.support.annotation.StringRes;
import android.view.View;
import android.widget.TextView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ViewUtil {

    private ViewUtil() {
    }

    public static void setText(TextView textView, @StringRes int textRes) {
        if (textView != null) {
            setText(textView, textView.getContext().getString(textRes));
        }
    }

    public static void setText(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    public static void show(boolean show, View... views) {
        if (views != null) {
            for (View v : views) {
                if (v != null) {
                    v.setVisibility(show ? VISIBLE : GONE);
                }
            }
        }
    }

    public static void enable(boolean enable, View... views) {
        if (views != null) {
            for (View v : views) {
                if (v != null) {
                    v.setEnabled(enable);
                }
            }
        }
    }

    public static boolean isShown(View view) {
        return view != null && view.getVisibility() == VISIBLE;
    }

}

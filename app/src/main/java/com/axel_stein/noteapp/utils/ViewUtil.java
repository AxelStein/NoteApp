package com.axel_stein.noteapp.utils;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;

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

    public static void show(View... views) {
        setVisible(true, views);
    }

    public static void hide(View... views) {
        setVisible(false, views);
    }

    public static void setVisible(boolean visible, View... views) {
        if (views != null) {
            for (View v : views) {
                if (v != null) {
                    v.setVisibility(visible ? VISIBLE : GONE);
                }
            }
        }
    }

    public static void enable(boolean enable, View... views) {
        if (views != null) {
            for (View v : views) {
                if (v != null) {
                    v.setEnabled(enable);

                    if (v instanceof ImageView) {
                        ImageView iv = (ImageView) v;

                        Drawable icon = iv.getDrawable();
                        if (icon != null) {
                            icon.mutate().setAlpha(enable ? 255 : 124);
                        }
                    }
                }
            }
        }
    }

    public static boolean isVisible(View view) {
        return view != null && view.getVisibility() == VISIBLE;
    }

    public static void toggleShow(View view) {
        setVisible(!isVisible(view), view);
    }

}

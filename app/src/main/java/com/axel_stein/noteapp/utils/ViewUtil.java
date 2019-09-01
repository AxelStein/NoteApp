package com.axel_stein.noteapp.utils;

import android.graphics.drawable.Drawable;
import androidx.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
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

    public static void show(View... views) {
        show(true, views);
    }

    public static void hide(View... views) {
        show(false, views);
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

    public static void invisible(boolean show, View... views) {
        if (views != null) {
            for (View v : views) {
                if (v != null) {
                    v.setVisibility(show ? VISIBLE : View.INVISIBLE);
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

    private static boolean isShown(View view) {
        return view != null && view.getVisibility() == VISIBLE;
    }

    public static void toggleShow(View view) {
        show(!isShown(view), view);
    }

}

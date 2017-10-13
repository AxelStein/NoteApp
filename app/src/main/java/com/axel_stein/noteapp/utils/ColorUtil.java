package com.axel_stein.noteapp.utils;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.TypedValue;

public class ColorUtil {

    @ColorInt
    public static int getColorAttr(@Nullable Context context, @AttrRes int colorAttr) {
        int color = 0;
        if (context != null) {
            try {
                TypedValue value = new TypedValue();
                context.getTheme().resolveAttribute(colorAttr, value, true);

                color = value.data;
            } catch (Exception ex) {
                ex.printStackTrace();
                color = 0;
            }
        }
        return color;
    }

}

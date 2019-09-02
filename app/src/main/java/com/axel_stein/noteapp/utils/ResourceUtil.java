package com.axel_stein.noteapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;

public class ResourceUtil {

    public static Drawable getDrawableTinted(Context context, @DrawableRes int drawableRes, @AttrRes int colorAttr) {
        Drawable drawable = context.getDrawable(drawableRes);
        if (drawable != null) {
            drawable.setTint(ColorUtil.getColorAttr(context, colorAttr));
        }
        return drawable;
    }

    public static String getString(Context context, String s, int res) {
        String result = s;
        if (res > 0) {
            try {
                result = context.getString(res);
            } catch (Resources.NotFoundException e) {
                result = s;
            }
        }
        return result;
    }

    public static String[] getStringArray(Context context, String[] array, int res) {
        String[] result = array;
        if (res > 0) {
            try {
                result = context.getResources().getStringArray(res);
            } catch (Resources.NotFoundException ignored) {
            }
        }
        return result;
    }

}

package com.axel_stein.noteapp.utils;

import android.content.Context;
import android.content.res.Resources;

public class ResourceUtil {

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
            } catch (Resources.NotFoundException e) {
                result = array;
            }
        }
        return result;
    }

}

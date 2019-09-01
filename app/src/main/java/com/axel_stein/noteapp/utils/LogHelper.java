package com.axel_stein.noteapp.utils;

import android.util.Log;

public class LogHelper {

    public static void logError(String msg, Exception e) {
        Log.e("TAG", msg, e);
    }

    private static void logVerbose(String msg) {
        Log.v("TAG", msg);
    }

    public static void logVerbose(String msg, Object... args) {
        logVerbose(parse(msg, args));
    }

    private static void logWarning(String msg) {
        Log.w("TAG", msg);
    }

    public static void logWarning(String msg, Object... args) {
        logWarning(parse(msg, args));
    }

    private static void logWtf(String msg) {
        Log.wtf("TAG", msg);
    }

    public static void logWtf(String msg, Object... args) {
        logWtf(parse(msg, args));
    }

    private static String parse(String msg, Object... args) {
        StringBuilder builder = new StringBuilder(msg);
        if (args != null) {
            for (Object o : args) {
                builder.append(" : ");
                builder.append(o);
            }
        }
        return builder.toString();
    }

}

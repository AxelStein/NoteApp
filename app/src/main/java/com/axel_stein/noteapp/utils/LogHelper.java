package com.axel_stein.noteapp.utils;

import android.util.Log;

public class LogHelper {

    static void logError(String msg, Exception e) {
        Log.e("TAG", msg, e);
    }

    static void logVerbose(String msg) {
        Log.v("TAG", msg);
    }

    static void logVerbose(String msg, Object... args) {
        logVerbose(parse(msg, args));
    }

    static void logWarning(String msg) {
        Log.w("TAG", msg);
    }

    static void logWarning(String msg, Object... args) {
        logWarning(parse(msg, args));
    }

    static void logWtf(String msg) {
        Log.wtf("TAG", msg);
    }

    static void logWtf(String msg, Object... args) {
        logWtf(parse(msg, args));
    }

    private static String parse(String msg, Object... args) {
        StringBuilder builder = new StringBuilder(msg);
        if (args != null) {
            for (Object o : args) {
                builder.append(" ");
                builder.append(String.valueOf(o));
            }
        }
        return builder.toString();
    }

}

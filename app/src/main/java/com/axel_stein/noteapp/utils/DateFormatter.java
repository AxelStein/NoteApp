package com.axel_stein.noteapp.utils;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_ABBREV_TIME;
import static android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_NO_MONTH_DAY;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;

public class DateFormatter {

    private DateFormatter() {
    }

    @NonNull
    public static String getMonthName(Context context, Long date) {
        if (context == null || date == null) {
            return "";
        }
        int flags = FORMAT_SHOW_DATE | FORMAT_NO_MONTH_DAY;
        return DateUtils.formatDateTime(context, date, flags);
    }

    @NonNull
    public static String formatDate(@Nullable Context context, Long date) {
        if (context == null || date == null) {
            return "";
        }
        return DateUtils.formatDateTime(context, date, FORMAT_SHOW_DATE | FORMAT_SHOW_WEEKDAY | FORMAT_ABBREV_WEEKDAY);
    }

    @NonNull
    public static String formatTime(@Nullable Context context, Long time) {
        if (context == null || time == null) {
            return "";
        }
        return DateUtils.formatDateTime(context, time, FORMAT_SHOW_TIME | FORMAT_ABBREV_TIME).toLowerCase();
    }

    @NonNull
    public static String formatDateTime(@Nullable Context context, Long time) {
        if (context == null || time == null) {
            return "";
        }
        return DateUtils.formatDateTime(context, time, FORMAT_SHOW_DATE | FORMAT_SHOW_TIME
                | FORMAT_ABBREV_TIME | FORMAT_ABBREV_MONTH).toLowerCase();
    }

}


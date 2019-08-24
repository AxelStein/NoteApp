package com.axel_stein.noteapp.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.format.DateUtils;

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

    public static String getMonthName(Context context, long date) {
        int flags = FORMAT_SHOW_DATE | FORMAT_NO_MONTH_DAY;
        return DateUtils.formatDateTime(context, date, flags);
    }

    @NonNull
    public static String formatDate(@Nullable Context context, long date) {
        if (context == null) {
            return "";
        }
        return DateUtils.formatDateTime(context, date, FORMAT_SHOW_DATE | FORMAT_SHOW_WEEKDAY | FORMAT_ABBREV_WEEKDAY);
    }

    @NonNull
    public static String formatTime(@Nullable Context context, long time) {
        if (context == null) {
            return "";
        }
        return DateUtils.formatDateTime(context, time, FORMAT_SHOW_TIME | FORMAT_ABBREV_TIME).toLowerCase();
    }

    @NonNull
    public static String formatDateTime(@Nullable Context context, long time) {
        if (context == null) {
            return "";
        }
        return DateUtils.formatDateTime(context, time, FORMAT_SHOW_DATE | FORMAT_SHOW_TIME
                | FORMAT_ABBREV_TIME | FORMAT_ABBREV_MONTH).toLowerCase();
    }


}


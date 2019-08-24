package com.axel_stein.data;


import androidx.room.TypeConverter;

import org.joda.time.DateTime;

public class Converters {

    @TypeConverter
    public static DateTime fromTimestamp(String value) {
        return value == null ? null : new DateTime(value);
    }

    @TypeConverter
    public static String dateToTimestamp(DateTime date) {
        return date == null ? null : date.toString();
    }

}

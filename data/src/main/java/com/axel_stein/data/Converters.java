package com.axel_stein.data;


import androidx.room.TypeConverter;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.MutableDateTime;

public class Converters {

    @TypeConverter
    public static DateTime fromTimestamp(String value) {
        return value == null ? null : new DateTime(value);
    }

    @TypeConverter
    public static MutableDateTime fromTimestamp_(String value) {
        return value == null ? null : new MutableDateTime(value);
    }

    @TypeConverter
    public static String dateTimeTostamp(DateTime date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static String dateTimeTostamp_(MutableDateTime date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static LocalDate fromLocalDatestamp(String value) {
        return value == null ? null : new LocalDate(value);
    }

    @TypeConverter
    public static String dateTostamp(LocalDate date) {
        return date == null ? null : date.toString();
    }

}

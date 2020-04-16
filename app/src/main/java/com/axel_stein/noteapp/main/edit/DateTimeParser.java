package com.axel_stein.noteapp.main.edit;

import org.joda.time.LocalDate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeParser {
    private static Pattern timePattern = Pattern.compile("[0-9]+:[0-9]+ ");
    private static Pattern datePattern = Pattern.compile("[0-9]+[.][0-9]+ ");
    private static Pattern dateYearPattern = Pattern.compile("[0-9]+[.][0-9]+[.][0-9]+ ");
    private int hours;
    private int minutes;
    private int day;
    private int month;
    private int year;
    private boolean noTime;
    private boolean noDate;

    public void parse(String input) {
        int[] time = parseImpl(timePattern, input, ":");
        if (time != null) {
            hours = time[0];
            minutes = time[1];
        } else {
            noTime = true;
            hours = 0;
            minutes = 0;
        }

        int[] date = parseImpl(dateYearPattern, input, "[.]");
        if (date != null) {
            day = date[0];
            month = date[1];
            year = date[2];
            if (year < 1000) {
                year += 2000;
            } else if (year > 3000) {
                year = new LocalDate().getYear();
            }
        } else {
            date = parseImpl(datePattern, input, "[.]");
            if (date != null) {
                day = date[0];
                month = date[1];
                year = new LocalDate().getYear();
            } else {
                noDate = true;
                day = 0;
                month = 0;
                year = 0;
            }
        }
        if (noDate) {
            LocalDate localDate = new LocalDate();
            day  = localDate.getDayOfMonth();
            month = localDate.getMonthOfYear();
            year = localDate.getYear();
        }
    }

    private int[] parseImpl(Pattern p, String input, String regex) {
        Matcher m = p.matcher(input);
        if (m.find()) {
            String[] arr = m.group().split(regex);
            int[] res = new int[arr.length];
            for (int i = 0; i < arr.length; i++) {
                res[i] = Integer.valueOf(arr[i].trim());
            }
            return res;
        }
        return null;
    }

    public boolean hasTime() {
        return !noTime;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

}

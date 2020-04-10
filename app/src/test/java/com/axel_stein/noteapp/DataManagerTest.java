package com.axel_stein.noteapp;

import com.axel_stein.domain.model.Note;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataManagerTest {
    private static Pattern timePattern = Pattern.compile("[0-9]+:[0-9]+");
    private static Pattern datePattern = Pattern.compile("[0-9]+[.][0-9]+");
    private static Pattern dateYearPattern = Pattern.compile("[0-9]+[.][0-9]+[.][0-9]+");

    private int hours;
    private int minutes;
    private int day;
    private int month;
    private int year;
    private boolean noTime;
    private boolean noDate;

    @Test
    public void newTest() {
        parse("8.7.2021 07:20 title");
        System.out.printf("%d:%d %d:%d:%d noTime=%b, noDate=%b", hours, minutes, day, month, year, noTime, noDate);
    }

    public void parse(String input) {
        int[] time = parseImpl(timePattern, input, ":");
        if (time != null) {
            hours = time[0];
            minutes = time[1];
        } else {
            noTime = true;
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
            }
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

    @Test
    public void t() {
        Pattern timePattern = Pattern.compile("[0-9]+:[0-9]+");
        Pattern simpleDatePattern = Pattern.compile("[0-9]+[.][0-9]+");
        Pattern yearDatePattern = Pattern.compile("[0-9]+[.][0-9]+[.][0-9]+");
        String input = "text 18.4 8:0";

        int[] time = parseImpl(timePattern, input, ":");
        System.out.println(Arrays.toString(time));

        int[] date = parseImpl(simpleDatePattern, input, "[.]");
        System.out.println(Arrays.toString(date));

        int[] dateYear = parseImpl(yearDatePattern, input, "[.]");
        System.out.println(Arrays.toString(dateYear));

        /*
        Matcher m = timePattern.matcher(input);
        if (m.find()) {
            String time = m.group();
            String[] arr = time.split(":");
            int hours = Integer.valueOf(arr[0]);
            int minutes = Integer.valueOf(arr[1]);
            System.out.println("hours = " + hours);
            System.out.println("minutes = " + minutes);
        }

        m = yearDatePattern.matcher(input);
        if (m.find()) {
            String date = m.group();
            String[] arr = date.split("[.]");
            int day = Integer.valueOf(arr[0]);
            int month = Integer.valueOf(arr[1]);
            int year = Integer.valueOf(arr[2]);
            System.out.println("day = " + day);
            System.out.println("month = " + month);
            System.out.println("year = " + year);
        } else {
            m = simpleDatePattern.matcher(input);
            if (m.find()) {
                String date = m.group();
                String[] arr = date.split("[.]");
                int day = Integer.valueOf(arr[0]);
                int month = Integer.valueOf(arr[1]);
                System.out.println("day = " + day);
                System.out.println("month = " + month);
            }
        }
        */

    }

    @Test
    public void test() {
        Note n1 = new Note();
        n1.setId("de3f");
        System.out.println("n1 = " + n1.hashCode());

        Note n2 = new Note();
        n2.setId("de4f");
        System.out.println("n2 = " + n2.hashCode());

        DateTime dateTime = new DateTime();
        System.out.println("now " + equalsNow(dateTime));
    }

    private boolean equalsNow(DateTime dateTime) {
        System.out.println("year " + dateTime.year().get());
        System.out.println("dayOfYear " + dateTime.dayOfYear().get());
        System.out.println("minuteOfDay " + dateTime.minuteOfDay().get());

        DateTime now = new DateTime(System.currentTimeMillis());
        boolean year = dateTime.year().equals(now.year());
        boolean dayOfYear = dateTime.dayOfYear().equals(now.dayOfYear());
        boolean minuteOfDay = dateTime.minuteOfDay().equals(now.minuteOfDay());
        return year && dayOfYear && minuteOfDay;
    }

}
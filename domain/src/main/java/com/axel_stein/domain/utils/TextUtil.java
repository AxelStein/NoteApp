package com.axel_stein.domain.utils;

public class TextUtil {

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean notEmpty(String s) {
        return !isEmpty(s);
    }

    public static String notNullString(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    public static String limitLength(String s) {
        if (s == null) return s;
        if (s.length() > 1024) {
            s = s.substring(0, 1024);
        }
        return s;
    }

    public static boolean contentEquals(String a, String b) {
        if (a != null && b != null) {
            return a.contentEquals(b);
        }
        return a == null && b == null;
    }

}

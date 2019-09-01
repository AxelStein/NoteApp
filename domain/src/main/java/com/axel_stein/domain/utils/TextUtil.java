package com.axel_stein.domain.utils;

public class TextUtil {

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean notEmpty(String s) {
        return !isEmpty(s);
    }

}

package com.axel_stein.domain.model;

import java.util.List;

public class LabelCache {

    private static List<Label> cache;

    public static void put(List<Label> value) {
        cache = value;
    }

    public static List<Label> get() {
        return cache;
    }

    public static boolean hasValue() {
        return cache != null;
    }

    public static void invalidate() {
        cache = null;
    }

}

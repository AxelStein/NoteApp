package com.axel_stein.domain.model;

import java.util.List;

public class NotebookCache {

    private static List<Notebook> cache;

    public static void put(List<Notebook> value) {
        cache = value;
    }

    public static List<Notebook> get() {
        return cache;
    }

    public static boolean hasValue() {
        return cache != null;
    }

    public static void invalidate() {
        cache = null;
    }

}

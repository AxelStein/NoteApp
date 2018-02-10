package com.axel_stein.domain.model;

import java.util.ArrayList;
import java.util.List;

public class NoteCache {

    private static LruCache<String, List<Note>> cache;

    public static void put(String key, List<Note> value) {
        if (cache == null) {
            cache = new LruCache<>(5);
        }
        cache.put(key, value);
    }

    public static List<Note> get(String key) {
        List<Note> result;
        if (cache != null) {
            result = cache.get(key);
        } else {
            result = new ArrayList<>();
        }
        return result;
    }

    public static boolean hasKey(String key) {
        return cache != null && cache.get(key) != null;
    }

    public static void invalidate() {
        cache = null;
    }

}

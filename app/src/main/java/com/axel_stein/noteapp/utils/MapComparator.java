package com.axel_stein.noteapp.utils;

import java.util.Map;
import java.util.Set;

public class MapComparator<K, V> {

    public boolean compare(Map<K, V> a, Map<K, V> b) {
        if (a == null && b == null) {
            return true;
        } else if (a != null && b != null) {
            if (a.size() != b.size()) {
                return false;
            }

            Set<K> keys = a.keySet();
            for (K k : keys) {
                if (!b.containsKey(k)) {
                    return false;
                }
                if (!equal(a.get(k), b.get(k))) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean equal(V a, V b) {
        return (a == null && b == null) || (a != null && b != null) && a.equals(b);
    }

}

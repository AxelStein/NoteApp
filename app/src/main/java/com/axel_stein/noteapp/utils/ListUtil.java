package com.axel_stein.noteapp.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class ListUtil {
    private static final String TAG = "ListUtil";

    public static <T> void log(List<T> list) {
        if (list != null) {
            fromStart(list, new Iteration<T>() {
                @Override
                public void next(int i, int count, T item) {
                    Log.d(TAG, i + " = " + String.valueOf(item));
                }
            });
        }
    }

    public static <T> void fromStart(List<T> list, Iteration<T> iteration) {
        if (list != null && iteration != null) {
            for (int i = 0, count = list.size(); i < count; i++) {
                iteration.next(i, count, list.get(i));
            }
        }
    }

    public static <T> void fromEnd(List<T> list, Iteration<T> iteration) {
        if (list != null && iteration != null) {
            for (int count = list.size(), i = count - 1; i >= 0; i--) {
                iteration.next(i, count, list.get(i));
            }
        }
    }

    @Nullable
    public static <T> T get(List<T> list, int i) {
        if (list != null) {
            if (i >= 0 && i < list.size()) {
                return list.get(i);
            }
        }
        return null;
    }

    public static <T> int remove(List<T> list, T t) {
        if (list != null) {
            int i = list.indexOf(t);
            if (i > 0 && i < list.size()) {
                list.remove(i);
                return i;
            }
        }
        return -1;
    }

    public interface Iteration<T> {
        void next(int i, int count, T item);
    }

}

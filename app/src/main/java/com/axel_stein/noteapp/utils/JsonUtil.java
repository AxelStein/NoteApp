package com.axel_stein.noteapp.utils;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonUtil {

    @Nullable
    public static String findString(JSONObject object, String name) {
        if (object != null) {
            if (object.has(name)) {
                return object.optString(name);
            }

            String result;

            Iterator<String> names = object.keys();
            if (names != null) {
                while (names.hasNext()) {
                    result = findString(object.optJSONObject(names.next()), name);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static JSONObject findObject(JSONObject object, String name) {
        if (object == null) {
            return null;
        } else if (object.has(name)) {
            return object.optJSONObject(name);
        }

        JSONObject result;

        Iterator<String> names = object.keys();
        if (names != null) {

            while (names.hasNext()) {
                result = findObject(object.optJSONObject(names.next()), name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Nullable
    public static JSONArray findArray(JSONObject object, String name) {
        if (object == null) {
            return null;
        } else if (object.has(name)) {
            return object.optJSONArray(name);
        }

        JSONArray result;

        Iterator<String> names = object.keys();
        if (names != null) {

            while (names.hasNext()) {
                result = findArray(object.optJSONObject(names.next()), name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

}

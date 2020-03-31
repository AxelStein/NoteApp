package com.axel_stein.noteapp.main.edit2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class CheckListHelper {

    @NonNull
    public static String toContentFromCheckList(@Nullable List<CheckItem> items) {
        if (items == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CheckItem item = items.get(i);
            if (item instanceof DataCheckItem) {
                continue;
            }
            if (item.isCheckable()) {
                builder.append(item.getText());
                if (i < items.size() - 1) {
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }

    @NonNull
    public static List<CheckItem> checkListFromContent(@Nullable String title, @Nullable String content) {
        content = content == null ? "" : content;
        List<CheckItem> items = new ArrayList<>();
        String[] lines = content.split("\n");

        CheckItem titleItem = new CheckItem(title);
        titleItem.setCheckable(false);
        items.add(titleItem);

        for (String line : lines) {
            items.add(new CheckItem(line));
        }

        if (items.size() == 1) {
            items.add(new CheckItem());
        }

        return items;
    }

    @NonNull
    public static List<CheckItem> fromJson(@Nullable String title, @Nullable String json) {
        //String list = "[{\"text\":\"milk\",\"checked\":false},{\"text\":\"eggs\",\"checked\":true}]";
        List<CheckItem> items = new ArrayList<>();

        CheckItem titleItem = new CheckItem(title);
        titleItem.setCheckable(false);
        items.add(titleItem);

        if (notEmpty(json)) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);
                    CheckItem item = new CheckItem();
                    item.setText(object.optString("text"));
                    item.setChecked(object.optBoolean("checked"));
                    items.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (items.size() == 1) {
            items.add(new CheckItem());
        }

        return items;
    }


    @NonNull
    public static String toJson(@Nullable List<CheckItem> items) {
        JSONArray array = new JSONArray();
        if (items != null) {
            for (CheckItem item : items) {
                if (item instanceof DataCheckItem) {
                    continue;
                }
                if (item.isCheckable()) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("checked", item.isChecked());
                        object.put("text", item.getText());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    array.put(object);
                }
            }
        }
        return array.toString();
    }

}

package com.axel_stein.data;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.repository.SettingsRepository;

import org.json.JSONException;
import org.json.JSONObject;

public class AppSettingsRepository implements SettingsRepository {
    public static final String PREF_NOTES_ORDER = "PREF_NOTES_ORDER";
    public static final String PREF_NIGHT_MODE = "PREF_NIGHT_MODE";
    public static final String PREF_FONT_SIZE = "PREF_FONT_SIZE";
    public static final String PREF_SHOW_NOTES_CONTENT = "PREF_SHOW_NOTES_CONTENT";
    public static final String PREF_SWIPE_LEFT_ACTION = "PREF_SWIPE_LEFT_ACTION";
    public static final String PREF_SWIPE_RIGHT_ACTION = "PREF_SWIPE_RIGHT_ACTION";
    private static final String PREF_BACKUP_FILE_DRIVE_ID = "PREF_BACKUP_FILE_DRIVE_ID";

    public static final int SWIPE_ACTION_NONE = 0;
    public static final int SWIPE_ACTION_TRASH_RESTORE = 1;
    public static final int SWIPE_ACTION_DELETE = 2;
    public static final int SWIPE_ACTION_PIN = 3;

    private SharedPreferences mPreferences;

    public AppSettingsRepository(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    @Override
    public void importSettings(String json) {
        try {
            JSONObject object = new JSONObject(json);

            setNightMode(object.optBoolean(PREF_NIGHT_MODE));
            setShowNotesContent(object.optBoolean(PREF_SHOW_NOTES_CONTENT));

            setPrefSwipeLeftAction(object.optString(PREF_SWIPE_LEFT_ACTION));
            setPrefSwipeRightAction(object.optString(PREF_SWIPE_RIGHT_ACTION));

            setPrefFontSize(object.optString(PREF_FONT_SIZE));

            setNotesOrder(NoteOrder.from(object.optInt(PREF_NOTES_ORDER)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void storeBackupFileDriveId(String id) {
        mPreferences.edit().putString(PREF_BACKUP_FILE_DRIVE_ID, id).apply();
    }

    @Nullable
    @Override
    public String getBackupFileDriveId() {
        return mPreferences.getString(PREF_BACKUP_FILE_DRIVE_ID, null);
    }

    @Override
    public String exportSettings() {
        JSONObject object = new JSONObject();

        try {
            object.put(PREF_NIGHT_MODE, nightMode());
            object.put(PREF_SHOW_NOTES_CONTENT, showNotesContent());

            object.put(PREF_SWIPE_LEFT_ACTION, getPrefSwipeLeftAction());
            object.put(PREF_SWIPE_RIGHT_ACTION, getPrefSwipeRightAction());

            object.put(PREF_FONT_SIZE, getPrefFontSize());

            NoteOrder order = getNotesOrder();
            object.put(PREF_NOTES_ORDER, order.ordinal());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public NoteOrder getNotesOrder() {
        int order = mPreferences.getInt(PREF_NOTES_ORDER, NoteOrder.TITLE.ordinal());
        return NoteOrder.from(order);
    }

    @Override
    public void setNotesOrder(NoteOrder order) {
        if (order != null) {
            mPreferences.edit().putInt(PREF_NOTES_ORDER, order.ordinal()).apply();
        }
    }

    @Override
    public boolean showNotesContent() {
        return mPreferences.getBoolean(PREF_SHOW_NOTES_CONTENT, true);
    }

    @Override
    public void setShowNotesContent(boolean show) {
        mPreferences.edit().putBoolean(PREF_SHOW_NOTES_CONTENT, show).apply();
    }

    private void setNightMode(boolean nightMode) {
        mPreferences.edit().putBoolean(PREF_NIGHT_MODE, nightMode).apply();
    }

    public boolean nightMode() {
        return mPreferences.getBoolean(PREF_NIGHT_MODE, false);
    }

    private void setPrefFontSize(String size) {
        mPreferences.edit().putString(PREF_FONT_SIZE, size).apply();
    }

    private String getPrefFontSize() {
        return mPreferences.getString(PREF_FONT_SIZE, "pref_font_size_default");
    }

    public int getBaseFontSize() {
        int baseSize = 16;

        String value = getPrefFontSize();
        switch (value) {
            case "pref_font_size_small":
                baseSize = 14;
                break;

            case "pref_font_size_default":
                baseSize = 16;
                break;

            case "pref_font_size_large":
                baseSize = 18;
                break;

            case "pref_font_size_extra_large":
                baseSize = 20;
                break;
        }

        return baseSize;
    }

    public boolean hasSwipeLeftAction() {
        String s = mPreferences.getString(PREF_SWIPE_LEFT_ACTION, "swipe_action_none");
        return !s.contentEquals("swipe_action_none");
    }

    public boolean hasSwipeRightAction() {
        String s = mPreferences.getString(PREF_SWIPE_RIGHT_ACTION, "swipe_action_none");
        return !s.contentEquals("swipe_action_none");
    }

    public int getSwipeLeftAction() {
        return wrapSwipeAction(getPrefSwipeLeftAction());
    }

    private void setPrefSwipeLeftAction(String action) {
        mPreferences.edit().putString(PREF_SWIPE_LEFT_ACTION, action).apply();
    }

    private String getPrefSwipeLeftAction() {
        return mPreferences.getString(PREF_SWIPE_LEFT_ACTION, "swipe_action_none");
    }

    public int getSwipeRightAction() {
        return wrapSwipeAction(getPrefSwipeRightAction());
    }

    private void setPrefSwipeRightAction(String action) {
        mPreferences.edit().putString(PREF_SWIPE_RIGHT_ACTION, action).apply();
    }

    private String getPrefSwipeRightAction() {
        return mPreferences.getString(PREF_SWIPE_RIGHT_ACTION, "swipe_action_none");
    }

    private int wrapSwipeAction(String s) {
        if (s != null) {
            switch (s) {
                case "swipe_action_trash_restore":
                    return SWIPE_ACTION_TRASH_RESTORE;

                case "swipe_action_delete":
                    return SWIPE_ACTION_DELETE;

                case "swipe_action_pin":
                    return SWIPE_ACTION_PIN;
            }
        }
        return SWIPE_ACTION_NONE;
    }

}

package com.axel_stein.data;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.axel_stein.domain.model.LabelOrder;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.NotebookOrder;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.SettingsRepository;

import org.json.JSONException;
import org.json.JSONObject;

public class AppSettingsRepository implements SettingsRepository {
    public static final String PREF_NOTES_ORDER = "PREF_NOTES_ORDER";
    public static final String PREF_NOTEBOOK_ORDER = "PREF_NOTEBOOK_ORDER";
    public static final String PREF_LABEL_ORDER = "PREF_LABEL_ORDER";
    public static final String PREF_NIGHT_MODE = "PREF_NIGHT_MODE";
    public static final String PREF_FONT_SIZE = "PREF_FONT_SIZE";
    public static final String PREF_SHOW_NOTES_CONTENT = "PREF_SHOW_NOTES_CONTENT";
    public static final String PREF_SHOW_NOTE_EDITOR_LINES = "PREF_SHOW_NOTE_EDITOR_LINES";
    public static final String PREF_SWIPE_LEFT_ACTION = "PREF_SWIPE_LEFT_ACTION";
    public static final String PREF_SWIPE_RIGHT_ACTION = "PREF_SWIPE_RIGHT_ACTION";
    public static final String PREF_CONTENT_CHAR_COUNTER = "PREF_CONTENT_CHAR_COUNTER";

    private static final String PREF_PASSWORD = "PREF_PASSWORD";

    // todo useless
    private static final String PREF_SHOW_EXIT_FULLSCREEN_MSG = "PREF_SHOW_EXIT_FULLSCREEN_MSG";
    private static final String PREF_SHOW_ADD_NOTE_FAB = "PREF_SHOW_ADD_NOTE_FAB";

    public static final int SWIPE_ACTION_NONE = 0;
    public static final int SWIPE_ACTION_TRASH_RESTORE = 1;
    public static final int SWIPE_ACTION_DELETE = 2;
    public static final int SWIPE_ACTION_PIN = 3;

    private SharedPreferences mPreferences;
    private DriveSyncRepository mDriveSyncRepository;

    private String mPassword; // fixme

    public AppSettingsRepository(SharedPreferences preferences, DriveSyncRepository d) {
        mPreferences = preferences;
        mDriveSyncRepository = d;
        mPassword = getPassword();
    }

    public void syncChanges() {
        mDriveSyncRepository.notifySettingsChanged(exportSettings());
    }

    private void importSettings(String json) {
        try {
            JSONObject object = new JSONObject(json);

            setNightMode(object.optBoolean(PREF_NIGHT_MODE));
            setShowNotesContent(object.optBoolean(PREF_SHOW_NOTES_CONTENT));

            setPrefSwipeLeftAction(object.optString(PREF_SWIPE_LEFT_ACTION));
            setPrefSwipeRightAction(object.optString(PREF_SWIPE_RIGHT_ACTION));

            setShowNoteEditorLines(object.optBoolean(PREF_SHOW_NOTE_EDITOR_LINES));
            enableContentCharCounter(object.optBoolean(PREF_CONTENT_CHAR_COUNTER));
            setPrefFontSize(object.optString(PREF_FONT_SIZE));

            setNotesOrder(NoteOrder.fromInt(object.optInt(PREF_NOTES_ORDER)));
            setNotebookOrder(NotebookOrder.fromInt(object.optInt(PREF_NOTEBOOK_ORDER)));
            setLabelOrder(LabelOrder.fromInt(object.optInt(PREF_LABEL_ORDER)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String exportSettings() {
        JSONObject object = new JSONObject();

        try {
            object.put(PREF_NIGHT_MODE, nightMode());
            object.put(PREF_SHOW_NOTES_CONTENT, showNotesContent());

            object.put(PREF_SWIPE_LEFT_ACTION, getPrefSwipeLeftAction());
            object.put(PREF_SWIPE_RIGHT_ACTION, getPrefSwipeRightAction());

            object.put(PREF_SHOW_NOTE_EDITOR_LINES, showNoteEditorLines());
            object.put(PREF_CONTENT_CHAR_COUNTER, contentCharCounterEnabled());
            object.put(PREF_FONT_SIZE, getPrefFontSize());

            object.put(PREF_NOTES_ORDER, getNotesOrder().ordinal());
            object.put(PREF_NOTEBOOK_ORDER, getNotebookOrder().ordinal());
            object.put(PREF_LABEL_ORDER, getLabelOrder().ordinal());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    public NoteOrder getNotesOrder() {
        return NoteOrder.fromInt(mPreferences.getInt(PREF_NOTES_ORDER, NoteOrder.TITLE.ordinal()));
    }

    @Override
    public void setNotesOrder(NoteOrder order) {
        mPreferences.edit().putInt(PREF_NOTES_ORDER, order.ordinal()).apply();
    }

    @Override
    public NotebookOrder getNotebookOrder() {
        return NotebookOrder.fromInt(mPreferences.getInt(PREF_NOTEBOOK_ORDER, NotebookOrder.TITLE.ordinal()));
    }

    @Override
    public void setNotebookOrder(NotebookOrder order) {
        mPreferences.edit().putInt(PREF_NOTEBOOK_ORDER, order.ordinal()).apply();
    }

    @Override
    public void setLabelOrder(LabelOrder order) {
        mPreferences.edit().putInt(PREF_LABEL_ORDER, order.ordinal()).apply();
    }

    @Override
    public LabelOrder getLabelOrder() {
        return LabelOrder.fromInt(mPreferences.getInt(PREF_LABEL_ORDER, LabelOrder.TITLE.ordinal()));
    }

    public boolean showAddNoteFAB() {
        return mPreferences.getBoolean(PREF_SHOW_ADD_NOTE_FAB, true);
    }

    public boolean showNoteEditorLines() {
        return mPreferences.getBoolean(PREF_SHOW_NOTE_EDITOR_LINES, false);
    }

    private void setShowNoteEditorLines(boolean show) {
        mPreferences.edit().putBoolean(PREF_SHOW_NOTE_EDITOR_LINES, show).apply();
    }

    @Override
    public boolean showNotesContent() {
        return mPreferences.getBoolean(PREF_SHOW_NOTES_CONTENT, true);
    }

    @Override
    public void setShowNotesContent(boolean show) {
        mPreferences.edit().putBoolean(PREF_SHOW_NOTES_CONTENT, show).apply();
    }

    public void setNightMode(boolean nightMode) {
        mPreferences.edit().putBoolean(PREF_NIGHT_MODE, nightMode).apply();
    }

    public boolean nightMode() {
        return mPreferences.getBoolean(PREF_NIGHT_MODE, false);
    }

    public void setShowExitFullscreenMessage(boolean show) {
        mPreferences.edit().putBoolean(PREF_SHOW_EXIT_FULLSCREEN_MSG, show).apply();
    }

    public boolean showExitFullscreenMessage() {
        boolean result = mPreferences.getBoolean(PREF_SHOW_EXIT_FULLSCREEN_MSG, true);
        if (result) {
            setShowExitFullscreenMessage(false);
        }
        return result;
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

    public void setPassword(@Nullable String password) {
        mPassword = password;
        mPreferences.edit().putString(PREF_PASSWORD, password).apply();
    }

    public boolean checkPassword(@Nullable String password) {
        return password != null && mPassword != null && TextUtils.equals(password, mPassword);
    }

    public boolean showPasswordInput() {
        return !TextUtils.isEmpty(mPassword);
    }

    @Nullable
    private String getPassword() {
        return mPreferences.getString(PREF_PASSWORD, null);
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

    public boolean contentCharCounterEnabled() {
        return mPreferences.getBoolean(PREF_CONTENT_CHAR_COUNTER, true);
    }

    public void enableContentCharCounter(boolean enable) {
        mPreferences.edit().putBoolean(PREF_CONTENT_CHAR_COUNTER, enable).apply();
    }

}

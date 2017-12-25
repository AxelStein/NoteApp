package com.axel_stein.data;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.NotebookOrder;
import com.axel_stein.domain.repository.SettingsRepository;

public class AppSettingsRepository implements SettingsRepository {
    private static final String PREF_NOTES_ORDER = "PREF_NOTES_ORDER";
    private static final String PREF_NOTEBOOK_ORDER = "PREF_NOTEBOOK_ORDER";
    private static final String PREF_NIGHT_MODE = "PREF_NIGHT_MODE";
    private static final String PREF_SHOW_EXIT_FULLSCREEN_MSG = "PREF_SHOW_EXIT_FULLSCREEN_MSG";
    private static final String PREF_FONT_SIZE = "PREF_FONT_SIZE";
    private static final String PREF_PASSWORD = "PREF_PASSWORD";
    private static final String PREF_SHOW_NOTES_CONTENT = "PREF_SHOW_NOTES_CONTENT";
    private static final String PREF_SHOW_NOTE_EDITOR_LINES = "PREF_SHOW_NOTE_EDITOR_LINES";
    private static final String PREF_SHOW_ADD_NOTE_FAB = "PREF_SHOW_ADD_NOTE_FAB";

    private SharedPreferences mPreferences;
    private String mDefaultNotebookTitle;
    private String mPassword;

    public AppSettingsRepository(SharedPreferences preferences, String defaultNotebookTitle) {
        mPreferences = preferences;
        mDefaultNotebookTitle = defaultNotebookTitle;
        mPassword = getPassword();
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
    public String defaultNotebookTitle() {
        return mDefaultNotebookTitle;
    }

    public boolean showAddNoteFAB() {
        return mPreferences.getBoolean(PREF_SHOW_ADD_NOTE_FAB, true);
    }

    public boolean showNoteEditorLines() {
        return mPreferences.getBoolean(PREF_SHOW_NOTE_EDITOR_LINES, false);
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

    public int getBaseFontSize() {
        int baseSize = 16;

        String value = mPreferences.getString(PREF_FONT_SIZE, null);
        if (value != null) {
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

}

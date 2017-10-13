package com.axel_stein.data;

import android.content.SharedPreferences;

import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.repository.SettingsRepository;

public class AppSettingsRepository implements SettingsRepository {

    private static final String PREF_NOTES_ORDER = "PREF_NOTES_ORDER";
    private static final String PREF_COUNTERS = "PREF_COUNTERS";
    private static final String PREF_NIGHT_MODE = "PREF_NIGHT_MODE";

    private SharedPreferences mPreferences;
    private String mDefaultNotebookTitle;

    public AppSettingsRepository(SharedPreferences preferences, String defaultNotebookTitle) {
        mPreferences = preferences;
        mDefaultNotebookTitle = defaultNotebookTitle;
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
    public String defaultNotebookTitle() {
        return mDefaultNotebookTitle;
    }

    @Override
    public boolean countersEnabled() {
        return mPreferences.getBoolean(PREF_COUNTERS, true);
    }

    @Override
    public void enableCounters(boolean enable) {
        mPreferences.edit().putBoolean(PREF_COUNTERS, enable).apply();
    }

    public void setNightMode(boolean nightMode) {
        mPreferences.edit().putBoolean(PREF_NIGHT_MODE, nightMode).apply();
    }

    public boolean nightMode() {
        return mPreferences.getBoolean(PREF_NIGHT_MODE, false);
    }

}

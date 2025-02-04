package com.axel_stein.data;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.repository.SettingsRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

import timber.log.Timber;

public class AppSettingsRepository implements SettingsRepository {
    public static final String BACKUP_FILE_NAME = "backup.json";
    private static final String PREF_DRIVE_AUTO_SYNC = "PREF_DRIVE_AUTO_SYNC";
    private static final String PREF_NOTES_ORDER = "PREF_NOTES_ORDER";
    public static final String PREF_NIGHT_MODE = "PREF_NIGHT_MODE";
    private static final String PREF_FONT_SIZE = "PREF_FONT_SIZE";
    public static final String PREF_SHOW_NOTES_CONTENT = "PREF_SHOW_NOTES_CONTENT";
    private static final String PREF_SWIPE_LEFT_ACTION = "PREF_SWIPE_LEFT_ACTION";
    private static final String PREF_SWIPE_RIGHT_ACTION = "PREF_SWIPE_RIGHT_ACTION";
    private static final String PREFS_ADS_ENABLED = "PREFS_ADS_ENABLED";
    private static final String PREFS_AD_PROPOSAL_ENABLED = "PREFS_AD_PROPOSAL_ENABLED";
    private static final String PREFS_AD_PROPOSAL_DISMISSED = "PREFS_AD_PROPOSAL_DISMISSED";

    public static final int SWIPE_ACTION_NONE = 0;
    public static final int SWIPE_ACTION_TRASH_RESTORE = 1;
    public static final int SWIPE_ACTION_PIN = 2;
    public static final int SWIPE_ACTION_STAR = 3;
    public static final int SWIPE_ACTION_ARCHIVE = 4;

    public interface OnEnableAdsListener {
        void onEnableAds(boolean enable);
    }

    private final SharedPreferences mPreferences;
    private final HashSet<OnEnableAdsListener> enableAdsListeners = new HashSet<>();

    public AppSettingsRepository(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    public void addOnEnableAdsListener(@Nullable OnEnableAdsListener listener) {
        if (listener == null) return;
        enableAdsListeners.add(listener);
    }

    public void removeOnEnableAdsListener(@Nullable OnEnableAdsListener listener) {
        if (listener == null) return;
        enableAdsListeners.remove(listener);
    }

    @Override
    public void importSettings(String json) {
        try {
            JSONObject object = new JSONObject(json);

            enableNightMode(object.optBoolean(PREF_NIGHT_MODE));
            setShowNotesContent(object.optBoolean(PREF_SHOW_NOTES_CONTENT));

            setPrefSwipeLeftAction(object.optString(PREF_SWIPE_LEFT_ACTION));
            setPrefSwipeRightAction(object.optString(PREF_SWIPE_RIGHT_ACTION));

            setPrefFontSize(object.optString(PREF_FONT_SIZE));
            setNotesOrder(NoteOrder.from(object.optInt(PREF_NOTES_ORDER)));
            enableAutoSync(object.optBoolean(PREF_DRIVE_AUTO_SYNC, true));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String exportSettings() {
        JSONObject object = new JSONObject();

        try {
            object.put(PREF_NIGHT_MODE, nightModeEnabled());
            object.put(PREF_SHOW_NOTES_CONTENT, showNotesContent());

            object.put(PREF_SWIPE_LEFT_ACTION, getPrefSwipeLeftAction());
            object.put(PREF_SWIPE_RIGHT_ACTION, getPrefSwipeRightAction());

            object.put(PREF_FONT_SIZE, getPrefFontSize());

            NoteOrder order = getNotesOrder();
            object.put(PREF_NOTES_ORDER, order.ordinal());
            object.put(PREF_DRIVE_AUTO_SYNC, autoSyncEnabled());
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

    public void enableAutoSync(boolean enable) {
        mPreferences.edit().putBoolean(PREF_DRIVE_AUTO_SYNC, enable).apply();
    }

    public boolean autoSyncEnabled() {
        return mPreferences.getBoolean(PREF_DRIVE_AUTO_SYNC, true);
    }

    private void enableNightMode(boolean enable) {
        mPreferences.edit().putBoolean(PREF_NIGHT_MODE, enable).apply();
    }

    public boolean nightModeEnabled() {
        return mPreferences.getBoolean(PREF_NIGHT_MODE, false);
    }

    public void setAdProposalEnabled(boolean enable) {
        mPreferences.edit().putBoolean(PREFS_AD_PROPOSAL_ENABLED, enable).commit();
        mPreferences.edit().putLong(PREFS_AD_PROPOSAL_DISMISSED, System.currentTimeMillis()).commit();
    }

    public boolean adProposalEnabled() {
        return mPreferences.getBoolean(PREFS_AD_PROPOSAL_ENABLED, true);
    }

    public void setAdsEnabled(boolean enable) {
        mPreferences.edit().putBoolean(PREFS_ADS_ENABLED, enable).commit();
        for (OnEnableAdsListener listener : enableAdsListeners) {
            listener.onEnableAds(enable);
        }
    }

    public boolean adsEnabled() {
        return mPreferences.getBoolean(PREFS_ADS_ENABLED, true);
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

                case "swipe_action_pin":
                    return SWIPE_ACTION_PIN;

                case "swipe_action_star":
                    return SWIPE_ACTION_STAR;

                case "swipe_action_archive":
                    return SWIPE_ACTION_ARCHIVE;
            }
        }
        return SWIPE_ACTION_NONE;
    }

}

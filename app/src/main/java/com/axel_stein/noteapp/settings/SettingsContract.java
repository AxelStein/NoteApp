package com.axel_stein.noteapp.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;

import java.io.File;

public interface SettingsContract {

    interface View {
        Context getContext();
        void startExportFileActivity(File file);
        void startImportFileActivity();
        void showImportDialog();
        void dismissImportDialog();
        void showMessage(int msg);
        void showMessage(String msg);
        void startRateAppActivity();
        void setAppVersion(String version);
    }

    interface Presenter {
        void onCreate(FragmentActivity activity);
        void onCreateView(View view);
        void onDestroyView();
        void onPreferenceChanged(SharedPreferences sharedPreferences, String s);
        void onPreferenceClick(String key);
        void onFileImport(String backup);
        void onDestroy();
    }

}

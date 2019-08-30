package com.axel_stein.noteapp;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.dagger.AppComponent;
import com.axel_stein.noteapp.dagger.AppModule;
import com.axel_stein.noteapp.dagger.DaggerAppComponent;
import com.axel_stein.noteapp.google_drive.DriveWorker;

import java.util.concurrent.TimeUnit;

public class App extends Application {

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notebook.TITLE_INBOX = getString(R.string.action_inbox);
        Notebook.ICON_INBOX = R.drawable.ic_inbox_white_24dp;

        sAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.contains("drive_work_id")) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            WorkRequest request = new PeriodicWorkRequest.Builder(DriveWorker.class, 1, TimeUnit.DAYS)
                    .setConstraints(constraints).build();
            WorkManager.getInstance(this).enqueue(request);

            preferences.edit().putString("drive_work_id", request.getId().toString()).apply();
        }
    }

}

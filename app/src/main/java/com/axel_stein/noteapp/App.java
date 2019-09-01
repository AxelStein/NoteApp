package com.axel_stein.noteapp;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.dagger.AppComponent;
import com.axel_stein.noteapp.dagger.AppModule;
import com.axel_stein.noteapp.dagger.DaggerAppComponent;
import com.axel_stein.noteapp.google_drive.DriveWorker;

public class App extends Application {

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notebook.TITLE_INBOX = getString(R.string.action_inbox);
        Notebook.ICON_INBOX = R.drawable.ic_inbox;

        sAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkRequest request = new OneTimeWorkRequest.Builder(DriveWorker.class).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueue(request);
    }

}

package com.axel_stein.noteapp;

import androidx.multidex.MultiDexApplication;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.dagger.AppComponent;
import com.axel_stein.noteapp.dagger.AppModule;
import com.axel_stein.noteapp.dagger.DaggerAppComponent;
import com.axel_stein.noteapp.google_drive.DriveWorker;

import javax.inject.Inject;

public class App extends MultiDexApplication {

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Inject
    AppSettingsRepository mSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        sAppComponent.inject(this);
        if (mSettings.autoSyncEnabled()) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            WorkRequest request = new OneTimeWorkRequest.Builder(DriveWorker.class).setConstraints(constraints).build();
            WorkManager.getInstance(this).enqueue(request);
        }
    }

}

package com.axel_stein.noteapp.google_drive;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

public class DriveWorker extends Worker {

    @Inject
    CreateBackupInteractor mCreateBackupInteractor;

    @Inject
    DriveServiceHelper mDriveServiceHelper;

    public DriveWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        App.getAppComponent().inject(this);
    }

    @SuppressLint("CheckResult")
    @NonNull
    @Override
    public Result doWork() {
        /*
        File dir = getApplicationContext().getFilesDir();
        File[] files = dir.listFiles();
        for (File file : files) {
            //file.getName();
            //file.lastModified();
        }
        */

        // find file
        // check last modified date
        // if date is not today -> create backup if error -> retry
        // check network
        // if true -> upload else -> retry
        //     if uploaded -> result success
        //     else retry

        /*
        mCreateBackupInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String backup) {
                        String fileName = "backup.json";

                        File dir = getApplicationContext().getFilesDir();
                        File file = writeToFile(dir, fileName, backup);

                        mDriveServiceHelper.upload(file);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
        */
        return Result.success();
    }

}

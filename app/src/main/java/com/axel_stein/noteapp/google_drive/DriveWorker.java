package com.axel_stein.noteapp.google_drive;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.utils.FileUtil;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;


import static com.axel_stein.data.AppSettingsRepository.BACKUP_FILE_NAME;
import static com.axel_stein.noteapp.utils.FileUtil.writeToFile;

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
        File dir = getApplicationContext().getFilesDir();
        File backupFile = FileUtil.findFile(dir, BACKUP_FILE_NAME);
        if (backupFile != null) {
            LocalDate date = new LocalDate(backupFile.lastModified());
            LocalDate today = new LocalDate();
            if (!date.equals(today)) {
                return uploadBackup();
            } else {
                return Result.success();
            }
        } else {
            return uploadBackup();
        }
    }

    private Result uploadBackup() {
        File dir = getApplicationContext().getFilesDir();
        File backupFile = writeToFile(dir, BACKUP_FILE_NAME, mCreateBackupInteractor.executeSync());
        try {
            mDriveServiceHelper.uploadBackupSync(backupFile);
            return Result.success();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.retry();
    }

}

package com.axel_stein.noteapp.google_drive;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.noteapp.App;

import org.joda.time.LocalDate;

import java.io.IOException;

import javax.inject.Inject;

import static com.axel_stein.data.AppSettingsRepository.BACKUP_FILE_NAME;

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
        Long val = mDriveServiceHelper.getFileModifiedDateSync(BACKUP_FILE_NAME);
        if (val != null) {
            LocalDate date = new LocalDate(val);
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
        try {
            mDriveServiceHelper.uploadFileSync(BACKUP_FILE_NAME, mCreateBackupInteractor.executeSync());
            return Result.success();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.retry();
    }

}

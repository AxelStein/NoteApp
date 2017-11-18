package com.axel_stein.domain.interactor.backup;

import com.axel_stein.domain.model.BackupFile;
import com.axel_stein.domain.repository.BackupFileRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class QueryBackupFileInteractor {

    private BackupFileRepository mBackupFileRepository;

    public QueryBackupFileInteractor(BackupFileRepository repository) {
        mBackupFileRepository = repository;
    }

    public Single<List<BackupFile>> execute() {
        return Single.fromCallable(new Callable<List<File>>() {
            @Override
            public List<File> call() throws Exception {
                return mBackupFileRepository.query();
            }
        }).map(new Function<List<File>, List<BackupFile>>() {
            @Override
            public List<BackupFile> apply(List<File> files) throws Exception {
                List<BackupFile> list = new ArrayList<>();
                for (File file : files) {
                    list.add(new BackupFile(file));
                }
                return list;
            }
        }).subscribeOn(Schedulers.io());
    }

}

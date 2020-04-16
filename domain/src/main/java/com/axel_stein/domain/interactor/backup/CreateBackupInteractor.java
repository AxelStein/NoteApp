package com.axel_stein.domain.interactor.backup;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Backup;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.repository.ReminderRepository;
import com.axel_stein.domain.repository.SettingsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class CreateBackupInteractor {

    @NonNull
    private final NoteRepository mNoteRepository;

    @NonNull
    private final NotebookRepository mNotebookRepository;

    @NonNull
    private final SettingsRepository mSettingsRepository;

    @NonNull
    private final ReminderRepository mReminderRepository;

    public CreateBackupInteractor(@NonNull NoteRepository n,
                                  @NonNull NotebookRepository b,
                                  @NonNull SettingsRepository s,
                                  @NonNull ReminderRepository r) {
        mNoteRepository = requireNonNull(n);
        mNotebookRepository = requireNonNull(b);
        mSettingsRepository = requireNonNull(s);
        mReminderRepository = requireNonNull(r);
    }

    public Single<String> execute() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                return executeSync();
            }
        }).subscribeOn(Schedulers.io());
    }

    public String executeSync() {
        Backup backup = new Backup();
        backup.setSourceNotes(mNoteRepository.queryAll());
        backup.setSourceNotebooks(mNotebookRepository.query());
        backup.setJsonSettings(mSettingsRepository.exportSettings());
        backup.setSourceReminders(mReminderRepository.query());
        try {
            return backup.toJson();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

}

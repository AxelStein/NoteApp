package com.axel_stein.domain.interactor.backup;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Backup;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.repository.SettingsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class CreateBackupInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NotebookRepository mNotebookRepository;

    @NonNull
    private SettingsRepository mSettingsRepository;

    public CreateBackupInteractor(@NonNull NoteRepository noteRepository,
                                  @NonNull NotebookRepository notebookRepository,
                                  @NonNull SettingsRepository settingsRepository) {
        mNoteRepository = requireNonNull(noteRepository);
        mNotebookRepository = requireNonNull(notebookRepository);
        mSettingsRepository = requireNonNull(settingsRepository);
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
        try {
            return backup.toJson();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

}

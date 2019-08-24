package com.axel_stein.domain.interactor.backup;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Backup;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.repository.SettingsRepository;

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
    private LabelRepository mLabelRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    @NonNull
    private SettingsRepository mSettingsRepository;

    public CreateBackupInteractor(@NonNull NoteRepository noteRepository,
                                  @NonNull NotebookRepository notebookRepository,
                                  @NonNull LabelRepository labelRepository,
                                  @NonNull NoteLabelPairRepository noteLabelPairRepository,
                                  @NonNull SettingsRepository settingsRepository) {
        mNoteRepository = requireNonNull(noteRepository);
        mNotebookRepository = requireNonNull(notebookRepository);
        mLabelRepository = requireNonNull(labelRepository);
        mNoteLabelPairRepository = requireNonNull(noteLabelPairRepository);
        mSettingsRepository = requireNonNull(settingsRepository);
    }

    public Single<String> execute() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Backup backup = new Backup();
                backup.setSourceNotes(mNoteRepository.queryAllTrashed());
                backup.setSourceNotebooks(mNotebookRepository.query());
                backup.setSourceLabels(mLabelRepository.query());
                backup.setNoteLabelPairs(mNoteLabelPairRepository.query());
                backup.setJsonSettings(mSettingsRepository.exportSettings());
                return backup.toJson();
            }
        }).subscribeOn(Schedulers.io());
    }

}

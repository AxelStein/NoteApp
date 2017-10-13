package com.axel_stein.domain.interactor.backup;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Backup;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class ExportBackupInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NotebookRepository mNotebookRepository;

    @NonNull
    private LabelRepository mLabelRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    public ExportBackupInteractor(@NonNull NoteRepository noteRepository,
                                  @NonNull NotebookRepository notebookRepository,
                                  @NonNull LabelRepository labelRepository,
                                  @NonNull NoteLabelPairRepository noteLabelPairRepository) {
        mNoteRepository = requireNonNull(noteRepository);
        mNotebookRepository = requireNonNull(notebookRepository);
        mLabelRepository = requireNonNull(labelRepository);
        mNoteLabelPairRepository = requireNonNull(noteLabelPairRepository);
    }

    public Single<String> execute() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Backup backup = new Backup();
                backup.setNotes(mNoteRepository.query());
                backup.setNotebooks(mNotebookRepository.query());
                backup.setLabels(mLabelRepository.query());
                backup.setNoteLabelPairs(mNoteLabelPairRepository.query());
                return backup.toJson();
            }
        }).subscribeOn(Schedulers.io());
    }

}

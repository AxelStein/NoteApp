package com.axel_stein.domain.interactor;

import android.support.annotation.NonNull;

import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class ResetInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NotebookRepository mNotebookRepository;

    @NonNull
    private LabelRepository mLabelRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    public ResetInteractor(@NonNull NoteRepository noteRepository,
                           @NonNull NotebookRepository notebookRepository,
                           @NonNull LabelRepository labelRepository,
                           @NonNull NoteLabelPairRepository noteLabelPairRepository) {
        mNoteRepository = requireNonNull(noteRepository);
        mNotebookRepository = requireNonNull(notebookRepository);
        mLabelRepository = requireNonNull(labelRepository);
        mNoteLabelPairRepository = requireNonNull(noteLabelPairRepository);
    }

    public Completable execute() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                mNoteRepository.deleteAll();
                mNotebookRepository.delete();
                mLabelRepository.delete();
                mNoteLabelPairRepository.deleteAll();
            }
        }).subscribeOn(Schedulers.io());
    }

}

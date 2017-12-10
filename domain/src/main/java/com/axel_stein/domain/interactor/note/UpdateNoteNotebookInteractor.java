package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.repository.NoteRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class UpdateNoteNotebookInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    public UpdateNoteNotebookInteractor(@NonNull NoteRepository noteRepository) {
        mNoteRepository = requireNonNull(noteRepository, "noteRepository is null");
    }

    public Completable execute(final long noteId, final long notebookId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (noteId <= 0) {
                    throw new IllegalArgumentException();
                }
                mNoteRepository.updateNotebook(noteId, notebookId);
            }
        }).subscribeOn(Schedulers.io());
    }

}

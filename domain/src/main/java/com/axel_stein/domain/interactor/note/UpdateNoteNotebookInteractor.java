package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.utils.TextUtil;

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

    public Completable execute(final String noteId, final long notebookId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (TextUtil.isEmpty(noteId)) {
                    throw new IllegalArgumentException();
                }
                mNoteRepository.updateNotebook(noteId, notebookId);
            }
        }).subscribeOn(Schedulers.io());
    }

}

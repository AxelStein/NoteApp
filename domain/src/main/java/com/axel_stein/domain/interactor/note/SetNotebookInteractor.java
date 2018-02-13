package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

// todo test
public class SetNotebookInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    public SetNotebookInteractor(@NonNull NoteRepository noteRepository) {
        mNoteRepository = requireNonNull(noteRepository, "noteStorage is null");
    }

    /**
     * @throws IllegalArgumentException if note`s id is 0 or notebookId is 0
     */
    public Completable execute(@NonNull final List<Note> notes, final long notebookId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notes)) {
                    throw new IllegalArgumentException("notes is not valid");
                }

                if (notebookId < 0) {
                    throw new IllegalArgumentException("notebookId is less than 0");
                }

                mNoteRepository.setNotebook(notes, notebookId);
            }
        }).subscribeOn(Schedulers.io());
    }

}

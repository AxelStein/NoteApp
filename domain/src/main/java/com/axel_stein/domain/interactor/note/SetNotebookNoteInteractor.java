package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class SetNotebookNoteInteractor {

    @NonNull
    private final NoteRepository mRepository;

    public SetNotebookNoteInteractor(@NonNull NoteRepository r) {
        mRepository = requireNonNull(r);
    }

    public Completable execute(final Note note, final String notebookId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                requireNonNull(note);
                String n = notebookId != null && notebookId.equals(Notebook.ID_INBOX) ? null : notebookId;
                mRepository.setNotebook(note.getId(), n);
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * @throws IllegalArgumentException if note`s id is 0 or notebookId is 0
     */
    public Completable execute(@NonNull final List<Note> notes, final String notebookId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                requireNonNull(notes);
                mRepository.setNotebook(notes, notebookId);
            }
        }).subscribeOn(Schedulers.io());
    }

}

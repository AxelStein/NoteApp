package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

public class SetStarredNoteInteractor {

    @NonNull
    private NoteRepository mRepository;

    public SetStarredNoteInteractor(@NonNull NoteRepository r) {
        mRepository = requireNonNull(r);
    }

    public Completable execute(@NonNull final Note note, final boolean starred) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(note)) {
                    throw new IllegalArgumentException("notes is not valid");
                }

                note.setStarred(starred);

                mRepository.setStarred(note, starred);
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable execute(@NonNull final List<Note> notes, final boolean starred) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notes)) {
                    throw new IllegalArgumentException("notes is not valid");
                }

                mRepository.setStarred(notes, starred);

                for (Note note : notes) {
                    note.setStarred(starred);
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}

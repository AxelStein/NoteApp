package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class SetPinnedNoteInteractor {

    @NonNull
    private final NoteRepository mRepository;

    public SetPinnedNoteInteractor(@NonNull NoteRepository r) {
        mRepository = requireNonNull(r);
    }

    public Completable execute(@NonNull final Note note, final boolean pinned) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                /*
                if (!isValid(note)) {
                    throw new IllegalArgumentException("notes is not valid");
                }
                */
                mRepository.setPinned(note, pinned);
                note.setPinned(pinned);
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable execute(@NonNull final List<Note> notes, final boolean pinned) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                /*
                if (!isValid(notes)) {
                    throw new IllegalArgumentException("notes is not valid");
                }
                */

                mRepository.setPinned(notes, pinned);

                for (Note note : notes) {
                    note.setPinned(pinned);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}

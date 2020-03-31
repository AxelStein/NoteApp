package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;

import org.joda.time.DateTime;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class InsertNoteInteractor {

    @NonNull
    private final NoteRepository mRepository;

    public InsertNoteInteractor(@NonNull NoteRepository r) {
        mRepository = requireNonNull(r);
    }

    /**
     * @param note to insert
     * @throws NullPointerException     if note is null
     * @throws IllegalArgumentException if notebook or note`s content is empty
     */
    public Completable execute(@NonNull final Note note) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                /*
                if (!validateBeforeInsert(note)) {
                    throw new IllegalArgumentException("note is not valid");
                }
                */

                note.setModifiedDate(new DateTime());

                mRepository.insert(note);
            }
        })
        .subscribeOn(Schedulers.io());
    }

}

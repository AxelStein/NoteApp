package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

public class SetPinnedNoteInteractor {

    @NonNull
    private NoteRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public SetPinnedNoteInteractor(@NonNull NoteRepository r, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(r);
        mDriveSyncRepository = requireNonNull(d);
    }

    public Completable execute(@NonNull final Note note, final boolean pinned) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(note)) {
                    throw new IllegalArgumentException("notes is not valid");
                }

                mRepository.setPinned(note, pinned);
                note.setPinned(pinned);

                mDriveSyncRepository.notePinned(note, pinned);
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable execute(@NonNull final List<Note> notes, final boolean pinned) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notes)) {
                    throw new IllegalArgumentException("notes is not valid");
                }

                mRepository.setPinned(notes, pinned);

                for (Note note : notes) {
                    note.setPinned(pinned);
                }

                mDriveSyncRepository.notesPinned(notes, pinned);
            }
        }).subscribeOn(Schedulers.io());
    }

}

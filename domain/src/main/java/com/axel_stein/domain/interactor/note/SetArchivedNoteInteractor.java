package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class SetArchivedNoteInteractor {

    @NonNull
    private final NoteRepository mRepository;

    public SetArchivedNoteInteractor(@NonNull NoteRepository r) {
        mRepository = requireNonNull(r);
    }

    public Completable execute(final Note note, final boolean pinned) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (note != null && note.hasId()) {
                    mRepository.setArchived(note.getId(), pinned);
                    note.setArchived(pinned);
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}

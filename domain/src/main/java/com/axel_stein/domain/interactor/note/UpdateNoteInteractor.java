package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NoteRepository;

import org.joda.time.DateTime;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.validateBeforeUpdate;

public class UpdateNoteInteractor {

    @NonNull
    private NoteRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public UpdateNoteInteractor(@NonNull NoteRepository r, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(r);
        mDriveSyncRepository = requireNonNull(d);
    }

    /**
     * @param note to update
     * @throws NullPointerException     if note is null
     * @throws IllegalArgumentException if id, notebook or note`s content is empty
     */
    public Completable execute(@NonNull final Note note) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!validateBeforeUpdate(note)) {
                    throw new IllegalArgumentException("note is note valid");
                }
                note.setModifiedDate(new DateTime());

                mRepository.update(note);
                mDriveSyncRepository.noteUpdated(note);
            }
        })
        .subscribeOn(Schedulers.io());
    }

}

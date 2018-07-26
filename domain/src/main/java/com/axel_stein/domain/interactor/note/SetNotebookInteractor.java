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

public class SetNotebookInteractor {

    @NonNull
    private NoteRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public SetNotebookInteractor(@NonNull NoteRepository r, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(r);
        mDriveSyncRepository = requireNonNull(d);
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

                mRepository.setNotebook(notes, notebookId);

                for (Note note : notes) {
                    mDriveSyncRepository.notifyNoteChanged(mRepository.get(note.getId()));
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}

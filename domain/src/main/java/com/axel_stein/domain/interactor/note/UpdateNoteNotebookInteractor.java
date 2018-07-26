package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.utils.TextUtil;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class UpdateNoteNotebookInteractor {

    @NonNull
    private NoteRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public UpdateNoteNotebookInteractor(@NonNull NoteRepository r, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(r);
        mDriveSyncRepository = requireNonNull(d);
    }

    public Completable execute(final String noteId, final long notebookId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (TextUtil.isEmpty(noteId)) {
                    throw new IllegalArgumentException();
                }

                mRepository.updateNotebook(noteId, notebookId);

                mDriveSyncRepository.notifyNoteChanged(mRepository.get(noteId));
            }
        }).subscribeOn(Schedulers.io());
    }

}

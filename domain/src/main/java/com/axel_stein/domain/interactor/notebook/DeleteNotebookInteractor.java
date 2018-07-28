package com.axel_stein.domain.interactor.notebook;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class DeleteNotebookInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NotebookRepository mNotebookRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public DeleteNotebookInteractor(@NonNull NoteRepository n, @NonNull NotebookRepository b, @NonNull DriveSyncRepository d) {
        mNoteRepository = requireNonNull(n);
        mNotebookRepository = requireNonNull(b);
        mDriveSyncRepository = requireNonNull(d);
    }

    /**
     * @param notebook to delete
     * @throws IllegalArgumentException if notebook == null, id <= 0 or title is empty
     */
    public Completable execute(@NonNull final Notebook notebook) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebook)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }
                mNotebookRepository.delete(notebook);
            }}).andThen(Completable.fromAction(new Action() {
                @Override
                public void run() {
                    mNoteRepository.deleteNotebook(notebook);
                }
            }))
            .andThen(Completable.fromAction(new Action() {
                @Override
                public void run() {
                    mNoteRepository.setInbox(notebook);
                }
            }))
            .andThen(Completable.fromAction(new Action() {
                @Override
                public void run() {
                    mDriveSyncRepository.notebookDeleted(notebook);
                }
            }))
            .subscribeOn(Schedulers.io());
    }

}

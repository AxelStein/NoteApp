package com.axel_stein.domain.interactor.notebook;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NotebookRepository;

import org.joda.time.DateTime;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class RenameNotebookInteractor {

    @NonNull
    private NotebookRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public RenameNotebookInteractor(@NonNull NotebookRepository r, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(r);
        mDriveSyncRepository = requireNonNull(d);
    }

    /**
     * @param notebook to update
     * @throws IllegalArgumentException if notebook is null, id <= 0 or title is empty
     */
    public Completable execute(@NonNull final Notebook notebook, final String title) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebook)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }

                notebook.setTitle(title);
                notebook.setModifiedDate(new DateTime());

                mRepository.update(notebook);
                mDriveSyncRepository.notebookRenamed(notebook);
            }
        }).subscribeOn(Schedulers.io());
    }
}

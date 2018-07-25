package com.axel_stein.domain.interactor.notebook;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NotebookRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class InsertNotebookInteractor {

    @NonNull
    private NotebookRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public InsertNotebookInteractor(@NonNull NotebookRepository n, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(n);
        mDriveSyncRepository = requireNonNull(d);
    }

    /**
     * Inserts notebook into repository
     *
     * @param notebook to insert
     * @throws IllegalArgumentException if notebook == null or title is empty
     */
    public Completable execute(@NonNull final Notebook notebook) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebook, false)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }
                notebook.setId(mRepository.insert(notebook));
                mDriveSyncRepository.notifyNotebooksChanged(mRepository.query());
            }
        }).subscribeOn(Schedulers.io());
    }

}

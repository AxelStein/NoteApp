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

public class UpdateNotebookInteractor {

    @NonNull
    private NotebookRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public UpdateNotebookInteractor(@NonNull NotebookRepository n, DriveSyncRepository d) {
        mRepository = requireNonNull(n);
        mDriveSyncRepository = requireNonNull(d);
    }

    /**
     * @param notebook to update
     * @throws NullPointerException     if notebook is null
     * @throws IllegalArgumentException if id is 0 or title is empty
     */
    public Completable execute(@NonNull final Notebook notebook) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebook)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }

                notebook.setModifiedDate(new DateTime());

                mRepository.update(notebook);
                mDriveSyncRepository.notebookUpdated(notebook);
            }
        }).subscribeOn(Schedulers.io());
    }

}

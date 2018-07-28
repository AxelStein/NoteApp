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

public class UpdateColorNotebookInteractor {

    @NonNull
    private NotebookRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public UpdateColorNotebookInteractor(@NonNull NotebookRepository r, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(r);
        mDriveSyncRepository = requireNonNull(d);
    }

    public Completable execute(final Notebook notebook) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebook)) {
                    throw new IllegalArgumentException("notebook not valid");
                }

                notebook.setModifiedDate(new DateTime());

                mRepository.update(notebook);
                mDriveSyncRepository.notebookColorChanged(notebook);
            }
        }).subscribeOn(Schedulers.io());
    }


}

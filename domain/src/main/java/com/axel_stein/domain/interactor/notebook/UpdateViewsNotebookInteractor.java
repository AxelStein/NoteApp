package com.axel_stein.domain.interactor.notebook;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NotebookRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class UpdateViewsNotebookInteractor {

    @NonNull
    private NotebookRepository mRepository;

    public UpdateViewsNotebookInteractor(@NonNull NotebookRepository r) {
        mRepository = requireNonNull(r);
    }

    public Completable execute(final Notebook notebook) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebook)) {
                    throw new IllegalArgumentException("notebook not valid");
                }
                mRepository.updateViews(notebook, notebook.incrementViews());
            }
        }).subscribeOn(Schedulers.io());
    }

}

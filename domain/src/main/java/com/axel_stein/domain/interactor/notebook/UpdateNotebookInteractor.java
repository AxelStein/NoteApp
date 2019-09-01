package com.axel_stein.domain.interactor.notebook;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NotebookRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class UpdateNotebookInteractor {

    @NonNull
    private final NotebookRepository mRepository;

    public UpdateNotebookInteractor(@NonNull NotebookRepository n) {
        mRepository = requireNonNull(n);
    }

    /**
     * @param notebook to update
     * @throws NullPointerException     if notebook is null
     * @throws IllegalArgumentException if id is 0 or title is empty
     */
    public Completable execute(@NonNull final Notebook notebook) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (!isValid(notebook)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }
                mRepository.update(notebook);
            }
        }).subscribeOn(Schedulers.io());
    }

}

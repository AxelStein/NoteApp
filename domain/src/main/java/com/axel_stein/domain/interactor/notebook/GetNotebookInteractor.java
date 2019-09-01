package com.axel_stein.domain.interactor.notebook;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NotebookRepository;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class GetNotebookInteractor {

    @NonNull
    private final NotebookRepository mNotebookRepository;

    public GetNotebookInteractor(@NonNull NotebookRepository notebookRepository) {
        mNotebookRepository = requireNonNull(notebookRepository, "notebookStorage is null");
    }

    /**
     * @param id notebook`s id
     * @return null, if there is no notebook with requested id
     * @throws IllegalStateException if notebook == null, id <= 0 or title is empty
     */
    public Single<Notebook> execute(final String id) {
        return Single.fromCallable(new Callable<Notebook>() {
            @Override
            public Notebook call() {
                Notebook notebook = mNotebookRepository.get(id);
                if (notebook != null) {
                    if (!isValid(notebook)) {
                        throw new IllegalStateException("notebook is not valid");
                    }
                } else {
                    notebook = new Notebook();
                }
                return notebook;
            }
        }).subscribeOn(Schedulers.io());
    }

}

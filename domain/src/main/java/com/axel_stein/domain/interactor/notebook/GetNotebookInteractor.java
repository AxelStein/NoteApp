package com.axel_stein.domain.interactor.notebook;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NotebookRepository;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class GetNotebookInteractor {

    @NonNull
    private NotebookRepository mNotebookRepository;

    public GetNotebookInteractor(@NonNull NotebookRepository notebookRepository) {
        mNotebookRepository = requireNonNull(notebookRepository, "notebookStorage is null");
    }

    /**
     * @param id notebook`s id
     * @return null, if there is no notebook with requested id
     * @throws IllegalStateException if notebook == null, id <= 0 or title is empty
     */
    @Nullable
    public Single<Notebook> execute(final long id) {
        return Single.fromCallable(new Callable<Notebook>() {
            @Override
            public Notebook call() throws Exception {
                Notebook notebook = mNotebookRepository.get(id);
                if (notebook != null) {
                    if (!isValid(notebook)) {
                        throw new IllegalStateException("notebook is not valid");
                    }
                }
                return notebook;
            }
        }).subscribeOn(Schedulers.io());
    }

}

package com.axel_stein.domain.interactor.notebook;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.model.NotebookCache;
import com.axel_stein.domain.repository.NotebookRepository;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class QueryNotebookInteractor {

    @NonNull
    private final NotebookRepository mNotebookRepository;

    public QueryNotebookInteractor(@NonNull NotebookRepository notebookRepository) {
        mNotebookRepository = requireNonNull(notebookRepository);
    }

    /**
     * @return all notebooks from repository
     * @throws IllegalStateException if result is not valid
     */
    @NonNull
    public Single<List<Notebook>> execute() {
        return Single.fromCallable(new Callable<List<Notebook>>() {
            @Override
            public List<Notebook> call() {
                if (NotebookCache.hasValue()) {
                    return NotebookCache.get();
                }

                List<Notebook> notebooks = mNotebookRepository.query();
                if (!isValid(notebooks)) {
                    throw new IllegalStateException("result is not valid");
                }

                NotebookCache.put(notebooks);

                return notebooks;
            }
        }).subscribeOn(Schedulers.io());
    }

}

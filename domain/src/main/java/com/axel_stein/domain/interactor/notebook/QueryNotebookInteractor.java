package com.axel_stein.domain.interactor.notebook;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.model.NotebookCache;
import com.axel_stein.domain.model.NotebookOrder;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.repository.SettingsRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class QueryNotebookInteractor {

    @NonNull
    private NotebookRepository mNotebookRepository;

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private SettingsRepository mSettingsRepository;

    public QueryNotebookInteractor(@NonNull NotebookRepository notebookRepository,
                                   @NonNull NoteRepository noteRepository,
                                   @NonNull SettingsRepository settingsRepository) {
        mNotebookRepository = requireNonNull(notebookRepository);
        mNoteRepository = requireNonNull(noteRepository);
        mSettingsRepository = requireNonNull(settingsRepository);
    }

    /**
     * @return all notebooks from repository
     * @throws IllegalStateException if result is not valid
     */
    @NonNull
    public Single<List<Notebook>> execute() {
        return Single.fromCallable(new Callable<List<Notebook>>() {
            @Override
            public List<Notebook> call() throws Exception {
                if (NotebookCache.hasValue()) {
                    return NotebookCache.get();
                }

                List<Notebook> notebooks = mNotebookRepository.query();
                if (!isValid(notebooks)) {
                    throw new IllegalStateException("result is not valid");
                }

                /*
                if (notebooks.size() == 0) {
                    Notebook notebook = new Notebook();
                    notebook.setTitle(mSettingsRepository.defaultNotebookTitle());
                    notebook.setId(mNotebookRepository.insert(notebook));

                    if (!isValid(notebook)) {
                        throw new IllegalStateException("notebook is not valid");
                    }

                    notebooks.add(notebook);
                }
                */

                for (Notebook notebook : notebooks) {
                    notebook.setNoteCount(mNoteRepository.count(notebook));
                }

                Collections.sort(notebooks, new Comparator<Notebook>() {
                    @Override
                    public int compare(Notebook n1, Notebook n2) {
                        NotebookOrder order = mSettingsRepository.getNotebookOrder();
                        switch (order) {
                            case TITLE:
                                return n1.getTitle().compareTo(n2.getTitle());

                            case CUSTOM:
                                return n1.getOrder() - n2.getOrder();
                        }
                        return 0;
                    }
                });

                NotebookCache.put(notebooks);

                return notebooks;
            }
        }).subscribeOn(Schedulers.io());
    }

}

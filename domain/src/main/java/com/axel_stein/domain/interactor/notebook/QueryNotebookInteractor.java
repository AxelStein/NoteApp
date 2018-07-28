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
            public List<Notebook> call() {
                if (NotebookCache.hasValue()) {
                    return NotebookCache.get();
                }

                List<Notebook> notebooks = mNotebookRepository.query();
                if (!isValid(notebooks)) {
                    throw new IllegalStateException("result is not valid");
                }

                for (Notebook notebook : notebooks) {
                    notebook.setNoteCount(mNoteRepository.count(notebook));
                }

                Collections.sort(notebooks, new Comparator<Notebook>() {
                    @Override
                    public int compare(Notebook n1, Notebook n2) {
                        NotebookOrder order = mSettingsRepository.getNotebookOrder();
                        boolean desc = order.isDesc();

                        switch (order) {
                            case TITLE:
                                if (desc) {
                                    return n2.getTitle().compareTo(n1.getTitle());
                                }
                                return n1.getTitle().compareTo(n2.getTitle());

                            case NOTE_COUNT:
                                if (desc) {
                                    return n1.getNoteCount() - n2.getNoteCount() > 0 ? 1 : -1;
                                }
                                return n1.getNoteCount() - n2.getNoteCount() > 0 ? -1 : 1;

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

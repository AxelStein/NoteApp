package com.axel_stein.domain.interactor.notebook;

import android.support.annotation.NonNull;

import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NotebookRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class DeleteNotebookInteractor {

    @NonNull
    private NotebookRepository mNotebookRepository;

    @NonNull
    private DeleteNoteInteractor mDeleteNoteInteractor;

    @NonNull
    private QueryNoteInteractor mQueryNoteInteractor;

    public DeleteNotebookInteractor(@NonNull NotebookRepository notebookRepository,
                                    @NonNull DeleteNoteInteractor deleteNotes,
                                    @NonNull QueryNoteInteractor queryNotes) {
        mNotebookRepository = requireNonNull(notebookRepository, "notebookStorage is null");
        mDeleteNoteInteractor = requireNonNull(deleteNotes, "deleteNotes is null");
        mQueryNoteInteractor = requireNonNull(queryNotes, "queryNotes is null");
    }

    /**
     * @param notebook to delete
     * @throws IllegalArgumentException if notebook == null, id <= 0 or title is empty
     */
    public Completable execute(@NonNull final Notebook notebook) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebook)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }
                mNotebookRepository.delete(notebook);
            }})
                .andThen(mQueryNoteInteractor.execute(notebook, true, false))
                .flatMapCompletable(new Function<List<Note>, CompletableSource>() {
                    @Override
                    public CompletableSource apply(@io.reactivex.annotations.NonNull List<Note> notes) throws Exception {
                        return mDeleteNoteInteractor.execute(notes);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

}

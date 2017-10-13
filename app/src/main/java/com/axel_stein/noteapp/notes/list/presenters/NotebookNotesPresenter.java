package com.axel_stein.noteapp.notes.list.presenters;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static android.support.v4.util.Preconditions.checkNotNull;

public class NotebookNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;
    private Notebook mNotebook;

    public NotebookNotesPresenter(Notebook notebook) {
        App.getAppComponent().inject(this);
        mNotebook = checkNotNull(notebook, "notebook is null");
    }

    @Override
    protected void load() {
        mInteractor.execute(mNotebook)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

}

package com.axel_stein.noteapp.main.list.presenters;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class NotebookNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;

    private String mNotebookId;

    public NotebookNotesPresenter(Notebook notebook) {
        App.getAppComponent().inject(this);
        if (notebook != null) {
            mNotebookId = notebook.getId();
        }
    }

    public NotebookNotesPresenter(String notebookId) {
        App.getAppComponent().inject(this);
        if (notEmpty(notebookId)) {
            mNotebookId = notebookId;
        }
    }

    @Override
    protected void load() {
        if (notEmpty(mNotebookId)) {
            mInteractor.query(mNotebookId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this);
        }
    }

}

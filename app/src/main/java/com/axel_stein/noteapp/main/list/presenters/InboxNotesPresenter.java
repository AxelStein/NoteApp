package com.axel_stein.noteapp.main.list.presenters;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class InboxNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;

    public InboxNotesPresenter() {
        App.getAppComponent().inject(this);
    }

    @Override
    protected void load() {
        mInteractor.queryInbox()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

}

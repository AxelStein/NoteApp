package com.axel_stein.noteapp.main.list.presenters;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class StarredNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;

    public StarredNotesPresenter() {
        App.getAppComponent().inject(this);
    }

    @Override
    protected void load() {
        mInteractor.queryStarred()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

}
package com.axel_stein.noteapp.notes.list.presenters;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class HomeNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;

    public HomeNotesPresenter() {
        App.getAppComponent().inject(this);
    }

    @Override
    protected void load() {
        mInteractor.executeHome()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

}

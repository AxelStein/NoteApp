package com.axel_stein.noteapp.notes.list.presenters;

import android.text.TextUtils;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class SearchNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;
    private String mQuery;

    public SearchNotesPresenter(String query) {
        App.getAppComponent().inject(this);
        mQuery = query;
    }

    @Override
    protected void load() {
        if (TextUtils.isEmpty(mQuery)) {
            onSuccess(null);
        } else {
            mInteractor.search(mQuery)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this);

            /*
            todo
            mInteractor.searchByTitle(mQuery)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this);
            */
        }
    }
}

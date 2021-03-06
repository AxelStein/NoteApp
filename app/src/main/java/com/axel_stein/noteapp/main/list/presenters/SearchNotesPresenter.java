package com.axel_stein.noteapp.main.list.presenters;

import android.text.TextUtils;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class SearchNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;
    private final String mQuery;

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
        }
    }
}

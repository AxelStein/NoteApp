package com.axel_stein.noteapp.notes.list.presenters;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static android.support.v4.util.Preconditions.checkNotNull;

public class LabelNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mQueryNoteInteractor;
    private Label mLabel;

    public LabelNotesPresenter(Label label) {
        App.getAppComponent().inject(this);
        mLabel = checkNotNull(label);
    }

    @Override
    protected void load() {
        mQueryNoteInteractor.execute(mLabel)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

}

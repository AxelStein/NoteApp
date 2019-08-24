package com.axel_stein.noteapp.main.list.presenters;

import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

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
        mQueryNoteInteractor.query(mLabel)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

}

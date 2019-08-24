package com.axel_stein.noteapp.main.list.presenters;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class TrashNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;

    public TrashNotesPresenter() {
        App.getAppComponent().inject(this);
    }

    @Override
    protected void load() {
        mInteractor.queryTrashed()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    @Override
    public int getCheckModeMenu() {
        return R.menu.action_mode_notes_trash;
    }

    @Override
    protected void handleSwipeAction(int action, Note note) {
        switch (action) {
            case AppSettingsRepository.SWIPE_ACTION_TRASH_RESTORE:
                restore(note);
                break;

            case AppSettingsRepository.SWIPE_ACTION_DELETE:
                delete(note);
                break;

            default:
                super.handleSwipeAction(action, note);
        }
    }

}

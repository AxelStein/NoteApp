package com.axel_stein.noteapp.notes.list.presenters;

import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.interactor.note.RestoreNoteInteractor;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

public class TrashNotesPresenter extends NotesPresenter {

    @Inject
    QueryNoteInteractor mInteractor;

    @Inject
    RestoreNoteInteractor mRestoreNoteInteractor;

    @Inject
    DeleteNoteInteractor mDeleteNoteInteractor;

    public TrashNotesPresenter() {
        App.getAppComponent().inject(this);
    }

    @Override
    protected void load() {
        mInteractor.queryTrash()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    @Override
    public int getCheckModeMenu() {
        return R.menu.action_mode_notes_trash;
    }

    @Override
    public void onActionItemClicked(int itemId) {
        super.onActionItemClicked(itemId);
        switch (itemId) {
            case R.id.menu_restore:
                mRestoreNoteInteractor.execute(getCheckedNotes())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                stopCheckMode();
                                EventBusHelper.showMessage(R.string.msg_notes_restored);
                                EventBusHelper.updateNoteList();
                            }
                        });
                break;

            case R.id.menu_delete:
                mView.showConfirmDeleteDialog();
                break;
        }
    }

    @Override
    public void confirmDelete() {
        mDeleteNoteInteractor.execute(getCheckedNotes())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        stopCheckMode();
                        EventBusHelper.showMessage(R.string.msg_notes_deleted);
                        EventBusHelper.updateNoteList();
                    }
                });
    }

}

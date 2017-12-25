package com.axel_stein.noteapp.notes.list.presenters;

import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.interactor.note.RestoreNoteInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;

import java.util.ArrayList;
import java.util.List;

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
    public void swipeLeft(Note note) {
        List<Note> list = new ArrayList<>();
        list.add(note);
        restore(list);
    }

    @Override
    public void onActionItemClicked(int itemId) {
        super.onActionItemClicked(itemId);
        switch (itemId) {
            case R.id.menu_restore:
                restore(getCheckedNotes());
                break;

            case R.id.menu_delete:
                mView.showConfirmDeleteDialog(getCheckedNotes());
                break;
        }
    }

    @Override
    public void confirmDelete() {
        final List<Note> notes = getCheckedNotes();
        mDeleteNoteInteractor.execute(notes)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        stopCheckMode();

                        int msg = notes.size() == 1 ? R.string.msg_note_deleted : R.string.msg_notes_deleted;
                        EventBusHelper.showMessage(msg);
                        EventBusHelper.updateNoteList();
                    }
                });
    }

}

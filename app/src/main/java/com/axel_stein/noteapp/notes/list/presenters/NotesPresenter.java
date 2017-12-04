package com.axel_stein.noteapp.notes.list.presenters;

import android.support.v4.util.LongSparseArray;

import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.label_helper.SetLabelsInteractor;
import com.axel_stein.domain.interactor.note.RestoreNoteInteractor;
import com.axel_stein.domain.interactor.note.SetNotebookInteractor;
import com.axel_stein.domain.interactor.note.TrashNoteInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notes.list.NotesContract;
import com.axel_stein.noteapp.notes.list.NotesContract.View;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static com.axel_stein.noteapp.utils.BooleanUtil.isTrue;
import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public abstract class NotesPresenter implements NotesContract.Presenter, SingleObserver<List<Note>> {

    protected View mView;

    @Inject
    TrashNoteInteractor mTrashNoteInteractor;

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Inject
    QueryLabelInteractor mQueryLabelInteractor;

    @Inject
    SetNotebookInteractor mSetNotebookInteractor;

    @Inject
    SetLabelsInteractor mSetLabelsInteractor;

    @Inject
    RestoreNoteInteractor mRestoreNoteInteractor;

    @Nullable
    private List<Note> mNotes;

    @Nullable
    private LongSparseArray<Boolean> mCheckedItems;

    @Override
    public void onCreateView(View view) {
        mView = checkNotNull(view);
        EventBusHelper.subscribe(this);
        App.getAppComponent().inject(this);

        if (mNotes != null) {
            mView.setNotes(mNotes);

            if (hasChecked()) {
                mView.startCheckMode();
                mView.onItemChecked(0, getCheckedCount());
            }
        } else {
            loadImpl();
        }
    }

    @Override
    public void onDestroyView() {
        EventBusHelper.unsubscribe(this);
        mView = null;
    }

    @Subscribe
    public void onUpdate(EventBusHelper.UpdateNoteList e) {
        loadImpl();
    }

    private void loadImpl() {
        if (mView != null) {
            load();
        }
    }

    protected abstract void load();

    @Override
    public void onSubscribe(@NonNull Disposable d) {
    }

    @Override
    public void onSuccess(@NonNull List<Note> notes) {
        if (mView != null) {
            mView.setNotes(notes);
        }
        mNotes = notes;
        stopCheckMode();
    }

    @Override
    public void onError(@NonNull Throwable e) {
        e.printStackTrace();
        if (mView != null) {
            mView.showError();
        }
    }

    @Override
    public void onNoteClick(int pos, Note note) {
        if (hasChecked()) {
            toggleCheck(pos, note);
        } else {
            mView.showNote(note);
        }
    }

    @Override
    public boolean onNoteLongClick(int pos, Note note) {
        if (!hasChecked()) {
            toggleCheck(pos, note);
            return true;
        }
        return false;
    }

    private void toggleCheck(int position, Note note) {
        if (mCheckedItems == null) {
            mCheckedItems = new LongSparseArray<>();
        }

        boolean startCheckMode = mCheckedItems.size() == 0;

        long id = note.getId();
        boolean checked = isTrue(mCheckedItems.get(id));
        checked = !checked;

        if (checked) {
            mCheckedItems.put(id, true);
        } else {
            mCheckedItems.delete(id);
        }

        if (startCheckMode) {
            mView.startCheckMode();
        } else if (mCheckedItems.size() == 0) {
            stopCheckMode();
            return;
        }

        mView.onItemChecked(position, getCheckedCount());
    }

    @Override
    public void stopCheckMode() {
        if (mView != null) {
            mView.stopCheckMode();
        }
        mCheckedItems = null;
    }

    @Override
    public boolean isChecked(Note note) {
        return mCheckedItems != null && isTrue(mCheckedItems.get(note.getId()));
    }

    @Override
    public boolean hasChecked() {
        return getCheckedCount() > 0;
    }

    private void checkAll() {
        int size = mNotes.size();
        if (getCheckedCount() == size) {
            return;
        }

        mCheckedItems = new LongSparseArray<>();
        for (Note note : mNotes) {
            mCheckedItems.put(note.getId(), true);
        }

        mView.onItemChecked(-1, size);
    }

    private int getCheckedCount() {
        return mCheckedItems == null ? 0 : mCheckedItems.size();
    }

    @Override
    public int getCheckModeMenu() {
        return R.menu.action_mode_notes;
    }

    @Override
    public void onActionItemClicked(int itemId) {
        switch (itemId) {
            case R.id.menu_select_all:
                checkAll();
                break;

            case R.id.menu_move_to_trash:
                moveToTrash(getCheckedNotes());
                break;

            case R.id.menu_select_notebook:
                mQueryNotebookInteractor.execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Notebook>>() {
                            @Override
                            public void accept(List<Notebook> notebooks) throws Exception {
                                if (mView != null) {
                                    mView.showSelectNotebookView(notebooks);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                EventBusHelper.showMessage(R.string.error);
                            }
                        });
                break;

            case R.id.menu_check_labels:
                mQueryLabelInteractor.execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Label>>() {
                            @Override
                            public void accept(List<Label> labels) throws Exception {
                                if (mView != null) {
                                    if (labels.size() == 0) {
                                        EventBusHelper.showMessage(R.string.msg_label_empty);
                                    } else {
                                        mView.showCheckLabelsView(labels);
                                    }
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                EventBusHelper.showMessage(R.string.error);
                            }
                        });
                break;
        }
    }

    protected void moveToTrash(final List<Note> notes) {
        if (notes == null) {
            return;
        }
        mTrashNoteInteractor.execute(notes)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        int msg = notes.size() == 1 ? R.string.msg_note_trashed : R.string.msg_notes_trashed;
                        EventBusHelper.showMessage(msg, R.string.action_undo, new Runnable() {
                            @Override
                            public void run() {
                                restore(notes);
                            }
                        });

                        stopCheckMode();
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    protected void restore(final List<Note> notes) {
        if (notes == null) {
            return;
        }
        mRestoreNoteInteractor.execute(notes)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        int msg = notes.size() == 1 ? R.string.msg_note_restored : R.string.msg_notes_restored;
                        EventBusHelper.showMessage(msg, R.string.action_undo, new Runnable() {
                            @Override
                            public void run() {
                                moveToTrash(notes);
                            }
                        });

                        stopCheckMode();
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    @Override
    public void onNotebookSelected(Notebook notebook) {
        final List<Note> notes = getCheckedNotes();
        mSetNotebookInteractor.execute(notes, notebook.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        stopCheckMode();

                        int msg = notes.size() == 1 ? R.string.msg_note_updated : R.string.msg_notes_updated;
                        EventBusHelper.showMessage(msg);
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    @Override
    public void onLabelsChecked(List<Long> labels) {
        final List<Note> notes = getCheckedNotes();
        mSetLabelsInteractor.execute(notes, labels)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        stopCheckMode();

                        int msg = notes.size() == 1 ? R.string.msg_note_updated : R.string.msg_notes_updated;
                        EventBusHelper.showMessage(msg);
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    protected List<Note> getCheckedNotes() {
        List<Note> notes = new ArrayList<>();
        if (mNotes != null && mCheckedItems != null) {
            for (Note note : mNotes) {
                if (isTrue(mCheckedItems.get(note.getId()))) {
                    notes.add(note);
                }
            }
        }
        return notes;
    }

    @Override
    public void confirmDelete() {

    }

    public List<Note> getNotes() {
        return mNotes;
    }

}

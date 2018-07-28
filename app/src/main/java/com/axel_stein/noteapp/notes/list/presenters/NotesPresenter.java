package com.axel_stein.noteapp.notes.list.presenters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.label_helper.SetLabelsInteractor;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.SetNotebookNoteInteractor;
import com.axel_stein.domain.interactor.note.SetPinnedNoteInteractor;
import com.axel_stein.domain.interactor.note.SetTrashedNoteInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteCache;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notes.list.NotesContract;
import com.axel_stein.noteapp.notes.list.NotesContract.View;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
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

    private View mView;

    @Inject
    SetTrashedNoteInteractor mSetTrashedNoteInteractor;

    @Inject
    DeleteNoteInteractor mDeleteNoteInteractor;

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Inject
    QueryLabelInteractor mQueryLabelInteractor;

    @Inject
    SetNotebookNoteInteractor mSetNotebookInteractor;

    @Inject
    SetLabelsInteractor mSetLabelsInteractor;

    @Inject
    SetPinnedNoteInteractor mSetPinnedNoteInteractor;

    @Inject
    AppSettingsRepository mSettings;

    @Nullable
    private List<Note> mNotes;

    @Nullable
    private HashMap<String, Boolean> mCheckedItems;

    private boolean mSortPanelEnabled = true;

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

            mView.showSortPanel(mSortPanelEnabled && notes != null && notes.size() > 0);
            if (notes != null) {
                mView.setSortPanelCounterText(notes.size());
            }

            int textRes = 0;

            NoteOrder order = mSettings.getNotesOrder();
            switch (order) {
                case TITLE:
                    textRes = R.string.action_sort_title;
                    break;

                case VIEWS:
                    textRes = R.string.action_sort_views;
                    break;

                case CREATED:
                    textRes = R.string.action_sort_created;
                    break;

                case MODIFIED:
                    textRes = R.string.action_sort_modified;
                    break;
            }

            mView.setSortTitle(textRes);
            mView.setSortIndicator(order.isDesc(), true);
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
            mCheckedItems = new HashMap<>();
        }

        boolean startCheckMode = mCheckedItems.size() == 0;

        String id = note.getId();
        boolean checked = isTrue(mCheckedItems.get(id));
        checked = !checked;

        if (checked) {
            mCheckedItems.put(id, true);
        } else {
            mCheckedItems.remove(id);
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

        mCheckedItems = new HashMap<>();
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

            case R.id.menu_pin_note:
                pin(getCheckedNotes());
                break;

            case R.id.menu_move_to_trash:
                moveToTrash(getCheckedNotes());
                break;

            case R.id.menu_delete:
                delete(getCheckedNotes());
                break;

            case R.id.menu_restore:
                restore(getCheckedNotes());
                break;

            case R.id.menu_select_notebook:
                mQueryNotebookInteractor.execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Notebook>>() {
                            @Override
                            public void accept(List<Notebook> notebooks) {
                                if (mView != null) {
                                    mView.showSelectNotebookView(notebooks);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
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
                            public void accept(List<Label> labels) {
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
                            public void accept(Throwable throwable) {
                                throwable.printStackTrace();
                                EventBusHelper.showMessage(R.string.error);
                            }
                        });
                break;
        }
    }

    @Override
    public boolean hasSwipeLeftAction() {
        return !hasChecked() && mSettings.hasSwipeLeftAction();
    }

    @Override
    public boolean hasSwipeRightAction() {
        return !hasChecked() && mSettings.hasSwipeRightAction();
    }

    @Override
    public void swipeLeft(Note note) {
        handleSwipeAction(mSettings.getSwipeLeftAction(), note);
    }

    @Override
    public void swipeRight(Note note) {
        handleSwipeAction(mSettings.getSwipeRightAction(), note);
    }

    protected void handleSwipeAction(int action, Note note) {
        switch (action) {
            case AppSettingsRepository.SWIPE_ACTION_NONE:
                break;

            case AppSettingsRepository.SWIPE_ACTION_TRASH_RESTORE:
                moveToTrash(note);
                break;

            case AppSettingsRepository.SWIPE_ACTION_DELETE:
                delete(note);
                break;

            case AppSettingsRepository.SWIPE_ACTION_PIN:
                pin(note);
                break;
        }
    }

    protected void pin(Note note) {
        List<Note> list = new ArrayList<>();
        list.add(note);
        pin(list);
    }

    @SuppressLint("CheckResult")
    protected void pin(final List<Note> notes) {
        boolean pin = false;
        for (Note note : notes) {
            if (!note.isPinned()) {
                pin = true;
                break;
            }
        }

        final boolean result = pin;
        Completable c = mSetPinnedNoteInteractor.execute(notes, result);
        c.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() {
                        int msg;
                        if (notes.size() == 1) {
                            msg = result ? R.string.msg_note_pinned : R.string.msg_note_unpinned;
                        } else {
                            msg = result ? R.string.msg_notes_pinned : R.string.msg_notes_unpinned;
                        }

                        EventBusHelper.showMessage(msg);
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    protected void moveToTrash(Note note) {
        moveToTrash(makeList(note));
    }

    @SuppressLint("CheckResult")
    protected void moveToTrash(final List<Note> notes) {
        if (notes == null) {
            return;
        }
        mSetTrashedNoteInteractor.execute(notes, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() {
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
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    protected void restore(Note note) {
        restore(makeList(note));
    }

    @SuppressLint("CheckResult")
    protected void restore(final List<Note> notes) {
        if (notes == null) {
            return;
        }
        mSetTrashedNoteInteractor.execute(notes, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() {
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
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    @Override
    public void onNotebookSelected(Notebook notebook) {
        if (notebook == null) {
            notebook = new Notebook();
        }
        onNotebookSelectedImpl(notebook);
    }

    @SuppressLint("CheckResult")
    private void onNotebookSelectedImpl(Notebook notebook) {
        final List<Note> notes = getCheckedNotes();
        mSetNotebookInteractor.execute(notes, notebook.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() {
                        stopCheckMode();

                        int msg = notes.size() == 1 ? R.string.msg_note_updated : R.string.msg_notes_updated;
                        EventBusHelper.showMessage(msg);
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void onLabelsChecked(List<String> labels) {
        final List<Note> notes = getCheckedNotes();
        mSetLabelsInteractor.execute(notes, labels)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() {
                        stopCheckMode();

                        int msg = notes.size() == 1 ? R.string.msg_note_updated : R.string.msg_notes_updated;
                        EventBusHelper.showMessage(msg);
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
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

    protected void delete(Note note) {
        delete(makeList(note));
    }

    protected void delete(List<Note> notes) {
        if (mView != null) {
            mView.showConfirmDeleteDialog(notes);
        }
    }

    public List<Note> getNotes() {
        return mNotes;
    }

    private List<Note> makeList(Note note) {
        List<Note> list = new ArrayList<>();
        list.add(note);
        return list;
    }

    @Override
    public void forceUpdate() {
        loadImpl();
    }

    protected void setSortPanelEnabled(boolean enabled) {
        mSortPanelEnabled = enabled;
    }

    @Override
    public void onSortTitleClick() {
        mSettings.toggleNoteDescOrder();
        updateNotesSort();
    }

    @Override
    public void onSortTitleLongClick() {
        if (mView != null) {
            mView.showSortDialog(menuItemFromOrder(mSettings.getNotesOrder()));
        }
    }

    @Override
    public void onSortMenuItemClick(MenuItem item) {
        mSettings.setNotesOrder(orderFromMenuItem(item));
        updateNotesSort();
    }

    private void updateNotesSort() {
        NoteCache.invalidate();
        EventBusHelper.updateNoteList();
        if (mView != null) {
            mView.scrollToTop();
        }
    }

    private NoteOrder orderFromMenuItem(MenuItem item) {
        if (item == null) {
            return null;
        }
        SparseArray<NoteOrder> sparseArray = new SparseArray<>();
        sparseArray.put(R.id.menu_sort_title, NoteOrder.TITLE);
        sparseArray.put(R.id.menu_sort_views, NoteOrder.VIEWS);
        sparseArray.put(R.id.menu_sort_created, NoteOrder.CREATED);
        sparseArray.put(R.id.menu_sort_modified, NoteOrder.MODIFIED);
        return sparseArray.get(item.getItemId());
    }

    private int menuItemFromOrder(NoteOrder order) {
        if (order == null) {
            return -1;
        }
        HashMap<NoteOrder, Integer> map = new HashMap<>();
        map.put(NoteOrder.TITLE, R.id.menu_sort_title);
        map.put(NoteOrder.VIEWS, R.id.menu_sort_views);
        map.put(NoteOrder.CREATED, R.id.menu_sort_created);
        map.put(NoteOrder.MODIFIED, R.id.menu_sort_modified);
        return map.get(order);
    }

}

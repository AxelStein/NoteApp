package com.axel_stein.noteapp.notes.edit;

import android.support.annotation.NonNull;

import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.InsertNoteInteractor;
import com.axel_stein.domain.interactor.note.RestoreNoteInteractor;
import com.axel_stein.domain.interactor.note.TrashNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notes.edit.EditNoteContract.OnNoteChangedListener;
import com.axel_stein.noteapp.notes.edit.EditNoteContract.View;
import com.axel_stein.noteapp.notes.edit.check_list.CheckItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static android.support.v4.util.Preconditions.checkNotNull;
import static android.text.TextUtils.isEmpty;

public class EditNotePresenter implements EditNoteContract.Presenter, CompletableObserver {
    @Inject
    InsertNoteInteractor mInsertNoteInteractor;
    @Inject
    UpdateNoteInteractor mUpdateNoteInteractor;
    @Inject
    TrashNoteInteractor mTrashNoteInteractor;
    @Inject
    RestoreNoteInteractor mRestoreNoteInteractor;
    @Inject
    DeleteNoteInteractor mDeleteNoteInteractor;
    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;
    @Inject
    QueryLabelInteractor mQueryLabelInteractor;
    private Note mNote;
    private Note mSrcNote;
    private View mView;
    private List<OnNoteChangedListener> mOnNoteChangedListeners;

    public EditNotePresenter(@NonNull Note note) {
        App.getAppComponent().inject(this);

        mNote = checkNotNull(note);
        mSrcNote = note.copy();
    }

    @Override
    public void addOnNoteChangedListener(OnNoteChangedListener l) {
        if (l == null) {
            return;
        }
        if (mOnNoteChangedListeners == null) {
            mOnNoteChangedListeners = new ArrayList<>();
        }
        mOnNoteChangedListeners.add(l);

        boolean notChanged = isEmptyNote() || mSrcNote.equals(mNote);
        l.onNoteChanged(!notChanged);
    }

    @Override
    public void convertCheckList() {
        if (mView == null) {
            return;
        }

        String content = mNote.getContent();
        if (content == null) {
            content = "";
        }

        List<CheckItem> items = new ArrayList<>();

        String[] array = content.split("\n");
        for (String s : array) {
            if (!isEmpty(s)) {
                CheckItem item = new CheckItem();
                item.setTitle(s);
                items.add(item);
            }
        }

        mView.showCheckList(items);
    }

    private void notifyChanged() {
        if (mOnNoteChangedListeners != null) {
            boolean notChanged = isEmptyNote() || mSrcNote.equals(mNote);
            for (OnNoteChangedListener l : mOnNoteChangedListeners) {
                if (l != null) {
                    l.onNoteChanged(!notChanged);
                }
            }
        }
    }

    @Override
    public void onCreateView(@NonNull View view) {
        mView = checkNotNull(view);
        mView.setNote(mNote);
    }

    @Override
    public void onDestroyView() {
        mView = null;
        mOnNoteChangedListeners = null;
    }

    @Override
    public void setTitle(String title) {
        mNote.setTitle(title);
        notifyChanged();
    }

    @Override
    public void setContent(String content) {
        mNote.setContent(content);
        notifyChanged();
    }

    @Override
    public boolean close() {
        if (isEmptyNote() || mSrcNote.equals(mNote)) {
            mView.callFinish();
            return true;
        } else {
            mView.showDiscardChangesView();
            return false;
        }
    }

    private boolean isEmptyNote() {
        return isEmpty(mNote.getTitle()) && isEmpty(mNote.getContent());
    }

    @Override
    public void confirmDiscardChanges() {
        mView.callFinish();
    }

    @Override
    public void save() {
        Completable completable;
        if (mNote.getId() > 0) {
            completable = mUpdateNoteInteractor.execute(mNote);
        } else {
            completable = mInsertNoteInteractor.execute(mNote);
        }
        completable.observeOn(AndroidSchedulers.mainThread()).subscribe(this);
    }

    @Override
    public void delete() {
        mDeleteNoteInteractor.execute(mNote)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (mView != null) {
                            mView.callFinish();
                        }
                        EventBusHelper.showMessage(R.string.msg_note_deleted);
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
    public void setNotebook(Notebook notebook) {
        mNote.setNotebook(notebook);
        notifyChanged();
    }

    @Override
    public void setLabels(List<Long> labels) {
        mNote.setLabels(labels);
        notifyChanged();
    }

    @Override
    public void actionMoveToTrash() {
        mTrashNoteInteractor.execute(mNote)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (mView != null) {
                            mView.callFinish();
                        }
                        EventBusHelper.showMessage(R.string.msg_note_trashed);
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
    public void actionSelectNotebook() {
        mQueryNotebookInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Notebook>>() {
                    @Override
                    public void accept(List<Notebook> notebooks) throws Exception {
                        if (mView != null) {
                            mView.showSelectNotebookView(notebooks, mNote.getNotebook());
                        }
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
    public void actionCheckLabels() {
        mQueryLabelInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Label>>() {
                    @Override
                    public void accept(List<Label> labels) throws Exception {
                        if (mView != null) {
                            if (labels.size() == 0) {
                                mView.showMessage(R.string.msg_label_empty);
                            } else {
                                mView.showCheckLabelsView(labels, mNote.getLabels());
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
    }

    @Override
    public void actionRestore() {
        mRestoreNoteInteractor.execute(mNote)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (mView != null) {
                            mView.callFinish();
                        }
                        EventBusHelper.showMessage(R.string.msg_note_restored);
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
    public void actionDelete() {
        if (mView != null) {
            mView.showConfirmDeleteNoteView();
        }
    }

    @Override
    public void actionShare() {
        if (mView != null) {
            if (isEmptyNote()) {
                mView.showMessage(R.string.msg_note_empty);
            } else {
                mView.showShareNoteView(mNote);
            }
        }
    }

    @Override
    public void actionInfo() {
        if (mView != null) {
            if (mNote.getId() <= 0 || isEmptyNote()) {
                mView.showMessage(R.string.msg_note_empty);
            } else {
                mView.showNoteInfoView(mNote);
            }
        }
    }

    @Override
    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

    }

    @Override
    public void onComplete() {
        if (mView != null) {
            mView.callFinish();
            EventBusHelper.showMessage(R.string.msg_note_updated);
            EventBusHelper.updateNoteList();
        }
    }

    @Override
    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
        e.printStackTrace();
        EventBusHelper.showMessage(R.string.error);
    }

}

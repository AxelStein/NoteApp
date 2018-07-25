package com.axel_stein.noteapp.notes.edit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.label_helper.SetLabelsInteractor;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.InsertNoteInteractor;
import com.axel_stein.domain.interactor.note.PinNoteInteractor;
import com.axel_stein.domain.interactor.note.RestoreNoteInteractor;
import com.axel_stein.domain.interactor.note.TrashNoteInteractor;
import com.axel_stein.domain.interactor.note.UnpinNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notes.edit.EditNoteContract.OnNoteChangedListener;
import com.axel_stein.noteapp.notes.edit.EditNoteContract.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static android.text.TextUtils.isEmpty;

public class EditNotePresenter implements EditNoteContract.Presenter {

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

    @Inject
    UpdateNoteNotebookInteractor mUpdateNoteNotebookInteractor;

    @Inject
    SetLabelsInteractor mSetLabelsInteractor;

    @Inject
    PinNoteInteractor mPinNoteInteractor;

    @Inject
    UnpinNoteInteractor mUnpinNoteInteractor;

    private Note mNote;
    private Note mSrcNote;
    private boolean mEditable;
    private boolean mSaving;

    @Nullable
    private View mView;

    private List<OnNoteChangedListener> mOnNoteChangedListeners;

    EditNotePresenter(@NonNull Note note) {
        App.getAppComponent().inject(this);

        mNote = note;
        mSrcNote = note.copy();
        mEditable = true;
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
        mView = view;
        mView.setNote(mNote);
        setEditableImpl(mEditable);
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
    public boolean onBackPressed() {
        if (mView != null && mView.searchPanelShown()){
            mView.hideSearchPanel();
            return false;
        } else {
            return close();
        }
    }

    @Override
    public boolean close() {
        if (mSaving) {
            return true;
        }
        if (isEmptyNote()) {
            return true;
        }
        if (mView == null) {
            return true;
        }
        if (mNote == null) {
            return true;
        }
        if (mSrcNote == null) {
            return true;
        }
        if (mSrcNote.equals(mNote)) {
            mView.callFinish();
            return true;
        } else {
            mView.showDiscardChangesView();
            return false;
        }
    }

    private boolean isEmptyNote() {
        return mNote != null && isEmpty(mNote.getTitle()) && isEmpty(mNote.getContent());
    }

    @Override
    public void confirmDiscardChanges() {
        if (mView != null) {
            mView.callFinish();
        }
    }

    @Override
    public void saveOrFinish() {
        if (mView == null) {
            return;
        }
        boolean notChanged = isEmptyNote() || mSrcNote.equals(mNote);
        if (notChanged) {
            mView.callFinish();
        } else {
            save();
        }
    }

    @Override
    public void save() {
        mSaving = true;
        setEditableImpl(false);

        Completable completable;

        final boolean hasId = mNote.hasId();
        if (hasId) {
            completable = mUpdateNoteInteractor.execute(mNote);
        } else {
            completable = mInsertNoteInteractor.execute(mNote);
        }

        completable.observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mSrcNote = mNote.copy();
                mSaving = false;

                setEditableImpl(true);

                if (mView != null) {
                    mView.setNote(mNote);
                    notifyChanged();
                    if (hasId) {
                        mView.showMessage(R.string.msg_note_updated);
                    } else {
                        mView.showMessage(R.string.msg_note_created);
                    }
                }

                EventBusHelper.updateNoteList();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();

                mSaving = false;
                setEditableImpl(true);

                if (mView != null) {
                    mView.showMessage(R.string.error);
                }
            }
        });
    }

    private void setEditableImpl(boolean enable) {
        mEditable = enable;
        if (mView != null) {
            mView.setEditable(enable);
        }
    }

    @Override
    public void delete() {
        mSaving = true;
        setEditableImpl(false);

        mDeleteNoteInteractor.execute(mNote)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        mSaving = false;
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

                        mSaving = false;
                        setEditableImpl(true);

                        if (mView != null) {
                            mView.showMessage(R.string.error);
                        }
                    }
                });
    }

    @Override
    public void actionPinNote() {
        if (!mNote.hasId()) {
            boolean p = !mNote.isPinned();

            mNote.setPinned(p);
            mSrcNote.setPinned(p);

            if (mView != null) {
                mView.setNotePinned(p);
            }

            notifyChanged();
            return;
        }

        Completable c;
        final boolean result = !mNote.isPinned();
        if (mNote.isPinned()) {
            c = mUnpinNoteInteractor.execute(mNote);
        } else {
            c = mPinNoteInteractor.execute(mNote);
        }
        c.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        mSrcNote.setPinned(result);

                        if (mView != null) {
                            mView.setNotePinned(result);
                            mView.showMessage(result ? R.string.msg_note_pinned : R.string.msg_note_unpinned);
                        }

                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void setNotebook(Notebook notebook) {
        if (notebook == null) {
            notebook = new Notebook();
        }
        setNotebookImpl(notebook);
    }

    private void setNotebookImpl(final Notebook notebook) {
        if (mNote.hasId()) {
            mUpdateNoteNotebookInteractor.execute(mNote.getId(), notebook.getId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            mNote.setNotebook(notebook);
                            mSrcNote.setNotebook(notebook);
                            if (mView != null) {
                                mView.showMessage(R.string.msg_note_updated);
                            }
                            EventBusHelper.updateNoteList();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            if (mView != null) {
                                mView.showMessage(R.string.error);
                            }
                        }
                    });
        } else {
            mNote.setNotebook(notebook);
            mSrcNote.setNotebook(notebook);

            notifyChanged();
        }
    }

    @Override
    public void setLabels(final List<Long> labels) {
        if (mNote.hasId()) {
            mSetLabelsInteractor.execute(mNote, labels)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            mNote.setLabels(labels);
                            mSrcNote.setLabels(labels);
                            if (mView != null) {
                                mView.showMessage(R.string.msg_note_updated);
                            }
                            EventBusHelper.updateNoteList();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            if (mView != null) {
                                mView.showMessage(R.string.error);
                            }
                        }
                    });
        } else {
            mNote.setLabels(labels);
            mSrcNote.setLabels(labels);

            notifyChanged();
        }
    }

    @Override
    public void actionMoveToTrash() {
        moveToTrash(mNote);
    }

    @Override
    public void actionRestore() {
        restore(mNote);
    }

    private void moveToTrash(final Note note) {
        if (note == null) {
            return;
        }

        mSaving = true;
        setEditableImpl(false);

        mTrashNoteInteractor.execute(note)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        mSaving = false;
                        if (mView != null) {
                            mView.callFinish();
                        }

                        EventBusHelper.showMessage(R.string.msg_note_trashed, R.string.action_undo, new Runnable() {
                            @Override
                            public void run() {
                                restore(note);
                            }
                        });
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                        mSaving = false;
                        setEditableImpl(true);

                        if (mView != null) {
                            mView.showMessage(R.string.error);
                        }
                    }
                });
    }

    private void restore(final Note note) {
        if (note == null) {
            return;
        }

        mSaving = true;
        setEditableImpl(false);

        mRestoreNoteInteractor.execute(note)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        mSaving = false;
                        if (mView != null) {
                            mView.callFinish();
                        }
                        EventBusHelper.showMessage(R.string.msg_note_restored, R.string.action_undo, new Runnable() {
                            @Override
                            public void run() {
                                moveToTrash(note);
                            }
                        });
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                        mSaving = false;
                        setEditableImpl(true);

                        if (mView != null) {
                            mView.showMessage(R.string.error);
                        }
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
                        if (mView != null) {
                            mView.showMessage(R.string.error);
                        }
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
                        if (mView != null) {
                            mView.showMessage(R.string.error);
                        }
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
    public void actionDuplicate(String copySuffix) {
        Note duplicate = mSrcNote.copy();
        duplicate.setId(null);

        String title = duplicate.getTitle();
        String content = duplicate.getContent();

        if (!isEmpty(title)) {
            duplicate.setTitle(String.format("%s (%s)", title, copySuffix));
        } else if (!isEmpty(content)) {
            duplicate.setContent(String.format("%s (%s)", content, copySuffix));
        }

        mInsertNoteInteractor.execute(duplicate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.showMessage(R.string.msg_note_duplicated);
                        }

                        EventBusHelper.updateNoteList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                        if (mView != null) {
                            mView.showMessage(R.string.error);
                        }
                    }
                });
    }

    @Override
    public void actionNoteInfo() {
        if (mView != null) {
            mView.showNoteInfo(mNote);
        }
    }

}

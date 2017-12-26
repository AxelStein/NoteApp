package com.axel_stein.noteapp.notebook_manager;

import android.support.v4.util.LongSparseArray;

import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notebook_manager.NotebookManagerContract.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.noteapp.utils.BooleanUtil.isTrue;

public class NotebookManagerPresenter implements NotebookManagerContract.Presenter, SingleObserver<List<Notebook>> {

    private View mView;

    private List<Notebook> mItems;

    @Nullable
    private LongSparseArray<Boolean> mCheckedItems;

    @Inject
    QueryNotebookInteractor mInteractor;

    public NotebookManagerPresenter() {
        App.getAppComponent().inject(this);
    }

    @Override
    public void onCreateView(View view) {
        mView = view;
        if (mItems != null) {
            mView.setItems(mItems);

            if (hasChecked()) {
                mView.startCheckMode();
                mView.onItemChecked(0, getCheckedCount());
            }
        } else {
            forceUpdate();
        }
    }

    @Override
    public void onDestroyView() {
        mView = null;
    }

    @Override
    public void forceUpdate() {
        mInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    @Override
    public void onItemClick(int pos, Notebook notebook) {
        if (checkModeEnabled()) {
            toggleCheck(pos, notebook);
        }
    }

    @Override
    public boolean onItemLongClick(int pos, Notebook notebook) {
        if (!checkModeEnabled()) {
            toggleCheck(pos, notebook);
            return true;
        }
        return false;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onSuccess(List<Notebook> notebooks) {
        mItems = notebooks;
        if (mView != null) {
            mView.setItems(mItems);
        }
        stopCheckMode();
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void startCheckMode() {
        mCheckedItems = new LongSparseArray<>();
        if (mView != null) {
            mView.startCheckMode();
            mView.onItemChecked(-1, 0);
        }
    }

    @Override
    public void stopCheckMode() {
        if (mView != null) {
            mView.stopCheckMode();
        }
        mCheckedItems = null;
    }

    @Override
    public boolean isChecked(Notebook notebook) {
        return mCheckedItems != null && isTrue(mCheckedItems.get(notebook.getId()));
    }

    @Override
    public boolean hasChecked() {
        return getCheckedCount() > 0;
    }

    @Override
    public void onActionItemClicked(int itemId) {
        switch (itemId) {
            case R.id.menu_select_all:
                checkAll();
                break;

            case R.id.menu_delete:
                if (mView != null) {
                    List<Notebook> list = getCheckedNotebooks();
                    if (list.size() > 0) {
                        mView.confirmDeleteDialog(list);
                    }
                }
                break;
        }
    }

    @Override
    public boolean checkModeEnabled() {
        return mCheckedItems != null;
    }

    private int getCheckedCount() {
        return mCheckedItems == null ? 0 : mCheckedItems.size();
    }

    private void toggleCheck(int position, Notebook notebook) {
        if (mCheckedItems == null) {
            mCheckedItems = new LongSparseArray<>();
        }

        boolean startCheckMode = mCheckedItems.size() == 0;

        long id = notebook.getId();
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

    private void checkAll() {
        int size = mItems.size();
        if (getCheckedCount() == size) {
            return;
        }

        mCheckedItems = new LongSparseArray<>();
        for (Notebook notebook : mItems) {
            mCheckedItems.put(notebook.getId(), true);
        }

        mView.onItemChecked(-1, size);
    }

    private List<Notebook> getCheckedNotebooks() {
        List<Notebook> notes = new ArrayList<>();
        if (mItems != null && mCheckedItems != null) {
            for (Notebook notebook : mItems) {
                if (isTrue(mCheckedItems.get(notebook.getId()))) {
                    notes.add(notebook);
                }
            }
        }
        return notes;
    }

}

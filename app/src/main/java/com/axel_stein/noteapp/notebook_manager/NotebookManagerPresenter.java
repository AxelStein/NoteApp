package com.axel_stein.noteapp.notebook_manager;

import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.notebook_manager.NotebookManagerContract.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class NotebookManagerPresenter implements NotebookManagerContract.Presenter, SingleObserver<List<Notebook>> {

    private View mView;

    private List<Notebook> mItems;

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
        if (mView != null) {
            mView.startNoteListActivity(notebook);
        }
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onSuccess(List<Notebook> notebooks) {
        mItems = new ArrayList<>(notebooks);
        mItems.add(0, Notebook.all());
        mItems.add(1, Notebook.starred());
        if (mView != null) {
            mView.setItems(mItems);
        }
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

}

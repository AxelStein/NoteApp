package com.axel_stein.noteapp.label_manager;

import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.label_manager.LabelManagerContract.View;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class LabelManagerPresenter implements LabelManagerContract.Presenter, SingleObserver<List<Label>> {

    private View mView;

    private List<Label> mItems;

    @Inject
    QueryLabelInteractor mInteractor;

    public LabelManagerPresenter() {
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
    public void onItemClick(int pos, Label label) {
        mView.startNoteListActivity(label);
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onSuccess(List<Label> labels) {
        mItems = labels;
        if (mView != null) {
            mView.setItems(mItems);
        }
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

}

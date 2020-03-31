package com.axel_stein.noteapp.utils;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

public class SimpleCompletableObserver implements CompletableObserver {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

}

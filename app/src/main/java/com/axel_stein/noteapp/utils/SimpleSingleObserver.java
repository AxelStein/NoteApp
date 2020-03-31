package com.axel_stein.noteapp.utils;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class SimpleSingleObserver<T> implements SingleObserver<T> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onSuccess(T t) {

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }
}

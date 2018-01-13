package com.axel_stein.domain.interactor.notebook;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.model.NotebookOrder;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.repository.SettingsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;

public class UpdateNotebookOrderInteractor {

    private NotebookRepository mRepository;

    private SettingsRepository mSettings;

    public UpdateNotebookOrderInteractor(@NonNull NotebookRepository repository, @NonNull SettingsRepository settings) {
        mRepository = requireNonNull(repository);
        mSettings = requireNonNull(settings);
    }

    public Completable execute(final List<Notebook> notebooks) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebooks)) {
                    throw new IllegalArgumentException("notebooks is not valid");
                }
                mSettings.setNotebookOrder(NotebookOrder.CUSTOM);
                for (int i = 0; i < notebooks.size(); i++) {
                    Notebook n = notebooks.get(i);
                    n.setOrder(i);
                    mRepository.update(n);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}
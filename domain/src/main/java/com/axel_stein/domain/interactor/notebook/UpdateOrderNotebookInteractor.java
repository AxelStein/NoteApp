package com.axel_stein.domain.interactor.notebook;

import androidx.annotation.NonNull;

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

public class UpdateOrderNotebookInteractor {

    @NonNull
    private NotebookRepository mRepository;

    @NonNull
    private SettingsRepository mSettingsRepository;

    public UpdateOrderNotebookInteractor(@NonNull NotebookRepository n, @NonNull SettingsRepository s) {
        mRepository = requireNonNull(n);
        mSettingsRepository = requireNonNull(s);
    }

    public Completable execute(final List<Notebook> notebooks) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notebooks)) {
                    throw new IllegalArgumentException();
                }

                mSettingsRepository.setNotebookOrder(NotebookOrder.CUSTOM);

                for (int i = 0; i < notebooks.size(); i++) {
                    Notebook notebook = notebooks.get(i);
                    notebook.setOrder(i);

                    mRepository.updateOrder(notebook, i);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}

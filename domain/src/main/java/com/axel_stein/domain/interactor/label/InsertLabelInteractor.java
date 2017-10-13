package com.axel_stein.domain.interactor.label;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.repository.LabelRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;

public class InsertLabelInteractor {

    @NonNull
    private LabelRepository mLabelRepository;

    public InsertLabelInteractor(@NonNull LabelRepository labelRepository) {
        mLabelRepository = requireNonNull(labelRepository, "labelStorage is null");
    }

    /**
     * @param label to insert
     * @throws IllegalArgumentException label is null or title is empty
     */
    public Completable execute(@NonNull final Label label) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(label, false)) {
                    throw new IllegalArgumentException("label is not valid");
                }
                label.setId(mLabelRepository.insert(label));
            }
        }).subscribeOn(Schedulers.io());
    }

}

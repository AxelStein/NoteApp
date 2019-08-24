package com.axel_stein.domain.interactor.label;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.repository.LabelRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;

public class UpdateViewsLabelInteractor {

    @NonNull
    private LabelRepository mRepository;

    public UpdateViewsLabelInteractor(@NonNull LabelRepository l) {
        mRepository = requireNonNull(l);
    }

    public Completable execute(final Label label) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(label)) {
                    throw new IllegalArgumentException("label not valid");
                }

                mRepository.updateViews(label, label.incrementViews());
            }
        }).subscribeOn(Schedulers.io());
    }

}

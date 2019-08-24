package com.axel_stein.domain.interactor.label;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.repository.LabelRepository;

import org.joda.time.DateTime;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;

public class UpdateLabelInteractor {

    @NonNull
    private LabelRepository mRepository;

    public UpdateLabelInteractor(@NonNull LabelRepository l) {
        mRepository = requireNonNull(l);
    }

    /**
     * @param label to update
     * @throws IllegalArgumentException if label is null, id <= 0 or title is empty
     */
    public Completable execute(@NonNull final Label label) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(label)) {
                    throw new IllegalArgumentException("label is not valid");
                }

                label.setModifiedDate(new DateTime());

                mRepository.update(label);
            }
        }).subscribeOn(Schedulers.io());
    }
}

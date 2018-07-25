package com.axel_stein.domain.interactor.label;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.LabelRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;

public class UpdateLabelInteractor {

    @NonNull
    private LabelRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public UpdateLabelInteractor(@NonNull LabelRepository l, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(l);
        mDriveSyncRepository = requireNonNull(d);
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
                mRepository.update(label);
                mDriveSyncRepository.notifyLabelsChanged(mRepository.query());
            }
        }).subscribeOn(Schedulers.io());
    }

}

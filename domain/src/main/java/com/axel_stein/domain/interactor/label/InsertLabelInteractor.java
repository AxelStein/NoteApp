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

public class InsertLabelInteractor {

    @NonNull
    private LabelRepository mRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public InsertLabelInteractor(@NonNull LabelRepository l, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(l);
        mDriveSyncRepository = requireNonNull(d);
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
                label.setId(mRepository.insert(label));
                mDriveSyncRepository.notifyLabelsChanged(mRepository.query());
            }
        }).subscribeOn(Schedulers.io());
    }

}

package com.axel_stein.domain.interactor.label;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.LabelOrder;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.SettingsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;

public class UpdateOrderLabelInteractor {

    @NonNull
    private LabelRepository mRepository;

    @NonNull
    private SettingsRepository mSettingsRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public UpdateOrderLabelInteractor(@NonNull LabelRepository l, @NonNull SettingsRepository s, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(l);
        mSettingsRepository = requireNonNull(s);
        mDriveSyncRepository = requireNonNull(d);
    }

    public Completable execute(final List<Label> labels) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(labels)) {
                    throw new IllegalArgumentException();
                }
                mSettingsRepository.setLabelOrder(LabelOrder.CUSTOM);
                for (int i = 0; i < labels.size(); i++) {
                    Label label = labels.get(i);
                    label.setOrder(i);

                    mRepository.updateOrder(label, i);
                    mDriveSyncRepository.labelOrderChanged(label);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}

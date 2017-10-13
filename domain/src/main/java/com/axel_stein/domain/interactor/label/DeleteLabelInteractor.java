package com.axel_stein.domain.interactor.label;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;

public class DeleteLabelInteractor {

    @NonNull
    private LabelRepository mLabelRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    public DeleteLabelInteractor(@NonNull LabelRepository labelRepository, @NonNull NoteLabelPairRepository helperRepository) {
        mLabelRepository = requireNonNull(labelRepository, "labelRepository is null");
        mNoteLabelPairRepository = requireNonNull(helperRepository, "helperRepository is null");
    }

    /**
     * Deletes label from repository
     *
     * @param label to delete
     * @throws IllegalArgumentException if label is null, id <= 0 or title is empty
     */
    public Completable execute(@NonNull final Label label) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(label)) {
                    throw new IllegalArgumentException("label is not valid");
                }
                mLabelRepository.delete(label);
                mNoteLabelPairRepository.delete(label);
            }
        }).subscribeOn(Schedulers.io());
    }

}

package com.axel_stein.domain.interactor.label;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.repository.LabelRepository;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;

public class GetLabelInteractor {

    @NonNull
    private LabelRepository mLabelRepository;

    public GetLabelInteractor(@NonNull LabelRepository labelRepository) {
        mLabelRepository = requireNonNull(labelRepository, "labelStorage is null");
    }

    /**
     * @param id label`s id
     * @throws IllegalStateException if label is null, id <= 0 or title is empty
     */
    public Single<Label> execute(final long id) {
        return Single.fromCallable(new Callable<Label>() {
            @Override
            public Label call() throws Exception {
                Label label = mLabelRepository.get(id);
                if (label != null) {
                    if (!isValid(label)) {
                        throw new IllegalStateException("label is not valid");
                    }
                }
                return label;
            }
        }).subscribeOn(Schedulers.io());
    }

}

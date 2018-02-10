package com.axel_stein.domain.interactor.label;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.LabelCache;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.SettingsRepository;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;

public class QueryLabelInteractor {

    @NonNull
    private LabelRepository mLabelRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    @NonNull
    private SettingsRepository mSettingsRepository;

    public QueryLabelInteractor(@NonNull LabelRepository labelRepository,
                                @NonNull NoteLabelPairRepository noteLabelPairRepository,
                                @NonNull SettingsRepository settingsRepository) {
        mLabelRepository = requireNonNull(labelRepository);
        mNoteLabelPairRepository = requireNonNull(noteLabelPairRepository);
        mSettingsRepository = requireNonNull(settingsRepository);
    }

    /**
     * @return all labels from storage
     * @throws IllegalStateException if query result is not valid
     */
    @NonNull
    public Single<List<Label>> execute() {
        return Single.fromCallable(new Callable<List<Label>>() {
            @Override
            public List<Label> call() throws Exception {
                if (LabelCache.hasValue()) {
                    return LabelCache.get();
                }

                List<Label> result = mLabelRepository.query();
                if (!isValid(result)) {
                    throw new IllegalStateException("result is not valid");
                }

                for (Label label : result) {
                    label.setNoteCount(mNoteLabelPairRepository.count(label));
                }

                LabelCache.put(result);

                return result;
            }
        }).subscribeOn(Schedulers.io());
    }

}

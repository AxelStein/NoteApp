package com.axel_stein.domain.interactor.label;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.LabelCache;
import com.axel_stein.domain.model.LabelOrder;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.SettingsRepository;

import java.util.Collections;
import java.util.Comparator;
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

                List<Label> labels = mLabelRepository.query();
                if (!isValid(labels)) {
                    throw new IllegalStateException("labels is not valid");
                }

                for (Label label : labels) {
                    label.setNoteCount(mNoteLabelPairRepository.count(label));
                }

                Collections.sort(labels, new Comparator<Label>() {
                    @Override
                    public int compare(Label l1, Label l2) {
                        LabelOrder order = mSettingsRepository.getLabelOrder();
                        switch (order) {
                            case TITLE:
                                return l1.getTitle().compareTo(l2.getTitle());

                            case NOTE_COUNT:
                                return l1.getNoteCount() - l2.getNoteCount() > 0 ? -1 : 1;

                            case CUSTOM:
                                return l1.getOrder() - l2.getOrder();
                        }
                        return 0;
                    }
                });


                LabelCache.put(labels);

                return labels;
            }
        }).subscribeOn(Schedulers.io());
    }

}

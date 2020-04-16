package com.axel_stein.domain.interactor.reminder;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Reminder;
import com.axel_stein.domain.repository.ReminderRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class UpdateReminderInteractor {

    @NonNull
    private final ReminderRepository mRepository;

    public UpdateReminderInteractor(@NonNull ReminderRepository r) {
        mRepository = requireNonNull(r);
    }

    public Completable execute(@NonNull final Reminder reminder) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                mRepository.update(reminder);
            }
        }).subscribeOn(Schedulers.io());
    }

    public void executeSync(Reminder reminder) {
        if (reminder != null) {
            mRepository.update(reminder);
        }
    }

}

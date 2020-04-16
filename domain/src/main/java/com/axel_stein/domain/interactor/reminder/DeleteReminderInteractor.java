package com.axel_stein.domain.interactor.reminder;

import androidx.annotation.NonNull;

import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.ReminderRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class DeleteReminderInteractor {

    @NonNull
    private final ReminderRepository mRepository;

    @NonNull
    private final NoteRepository mNoteRepository;

    public DeleteReminderInteractor(@NonNull ReminderRepository r, @NonNull NoteRepository nr) {
        mRepository = requireNonNull(r);
        mNoteRepository = requireNonNull(nr);
    }

    public Completable execute(@NonNull final String reminderId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                mRepository.delete(reminderId);
                mNoteRepository.deleteReminder(reminderId);
            }
        }).subscribeOn(Schedulers.io());
    }

}

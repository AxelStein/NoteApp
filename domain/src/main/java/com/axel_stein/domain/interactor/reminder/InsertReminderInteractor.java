package com.axel_stein.domain.interactor.reminder;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Reminder;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.ReminderRepository;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class InsertReminderInteractor {

    @NonNull
    private final ReminderRepository mRepository;

    @NonNull
    private final NoteRepository mNoteRepository;

    public InsertReminderInteractor(@NonNull ReminderRepository r, @NonNull NoteRepository n) {
        mRepository = requireNonNull(r);
        mNoteRepository = requireNonNull(n);
    }

    public Completable execute(@NonNull final Reminder reminder, @NonNull final String noteId) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                mRepository.insert(reminder);
                mNoteRepository.setReminder(noteId, reminder.getId());
            }
        }).subscribeOn(Schedulers.io());
    }

}

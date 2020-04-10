package com.axel_stein.domain.interactor.reminder;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Reminder;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.ReminderRepository;

import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class GetReminderInteractor {
    @NonNull
    private final ReminderRepository mRepository;

    @NonNull
    private final NoteRepository mNoteRepository;

    public GetReminderInteractor(@NonNull ReminderRepository r, @NonNull NoteRepository n) {
        this.mRepository = requireNonNull(r);
        this.mNoteRepository = requireNonNull(n);
    }

    public Reminder executeSync(final String reminderId) {
        return mRepository.get(reminderId);
    }

    public Single<Reminder> findByNoteId(final String noteId) {
        return Single.fromCallable(new Callable<Reminder>() {
            @Override
            public Reminder call() {
                String reminderId = mNoteRepository.getReminderId(noteId);
                if (notEmpty(reminderId)) {
                    return mRepository.get(reminderId);
                }
                return new Reminder();
            }
        }).subscribeOn(Schedulers.io());
    }

    public List<Reminder> query() {
        return mRepository.query();
    }

    public List<Reminder> queryDateTime(DateTime dateTime) {
        return mRepository.queryDateTime(dateTime);
    }

    public Reminder getNextAfter(DateTime dateTime) {
        List<Reminder> list = mRepository.query();
        for (Reminder r : list) {
            if (r.getDateTime().isAfter(dateTime)) {
                return r;
            }
        }
        return null;
    }

}

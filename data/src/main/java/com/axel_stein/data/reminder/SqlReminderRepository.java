package com.axel_stein.data.reminder;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Reminder;
import com.axel_stein.domain.repository.ReminderRepository;

import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class SqlReminderRepository implements ReminderRepository {
    @NonNull
    private final ReminderDao mDao;

    public SqlReminderRepository(@NonNull ReminderDao mDao) {
        this.mDao = mDao;
    }

    @Override
    public void insert(@NonNull Reminder reminder) {
        if (!reminder.hasId()) {
            reminder.setId(UUID.randomUUID().toString());
        }
        mDao.insert(ReminderMapper.map(reminder));
    }

    @Override
    public void update(@NonNull Reminder reminder) {
        mDao.update(ReminderMapper.map(reminder));
    }

    @Override
    public Reminder get(@NonNull String id) {
        return ReminderMapper.map(mDao.get(id));
    }

    @Override
    public void delete(@NonNull String reminderId) {
        mDao.delete(reminderId);
    }

    @Override
    public List<Reminder> query() {
        return mDao.query();
    }

    @Override
    public List<Reminder> queryDateTime(DateTime dateTime) {
        return mDao.queryDateTime(dateTime);
    }
}

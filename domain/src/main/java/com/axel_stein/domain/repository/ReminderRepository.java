package com.axel_stein.domain.repository;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Reminder;

import org.joda.time.DateTime;

import java.util.List;

public interface ReminderRepository {

    void insert(@NonNull Reminder reminder);

    void update(@NonNull Reminder reminder);

    Reminder get(@NonNull String id);

    void delete(@NonNull String reminderId);

    List<Reminder> query();

    List<Reminder> queryDateTime(DateTime dateTime);

}

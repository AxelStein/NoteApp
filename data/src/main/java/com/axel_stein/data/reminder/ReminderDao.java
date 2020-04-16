package com.axel_stein.data.reminder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.axel_stein.domain.model.Reminder;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    void insert(@NonNull ReminderEntity reminder);

    @Update
    void update(@NonNull ReminderEntity reminder);

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    @Nullable
    ReminderEntity get(String id);

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    void delete(@NonNull String reminderId);

    @Query("SELECT * FROM reminders ORDER BY dateTime")
    List<Reminder> query();

    @Query("SELECT * FROM reminders WHERE dateTime = :dateTime ORDER BY dateTime")
    List<Reminder> queryDateTime(DateTime dateTime);
}

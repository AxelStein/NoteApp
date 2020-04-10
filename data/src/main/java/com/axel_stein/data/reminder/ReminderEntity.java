package com.axel_stein.data.reminder;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

@Entity(tableName = "reminders")
public class ReminderEntity {
    @PrimaryKey
    @NonNull
    private String id = "";

    @ColumnInfo
    private String noteId;

    @ColumnInfo
    private DateTime dateTime;

    @ColumnInfo
    private int repeatMode;

    @ColumnInfo
    private int repeatCount;

    @ColumnInfo
    private LocalDate repeatEndDate;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public LocalDate getRepeatEndDate() {
        return repeatEndDate;
    }

    public void setRepeatEndDate(LocalDate repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
    }
}

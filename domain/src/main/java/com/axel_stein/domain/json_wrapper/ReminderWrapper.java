package com.axel_stein.domain.json_wrapper;

import com.axel_stein.domain.model.Reminder;
import com.axel_stein.domain.utils.TextUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.UUID;

public class ReminderWrapper {
    private String id;
    private String noteId;
    private DateTime dateTime;
    private int repeatMode;
    private int repeatCount;
    private LocalDate repeatEndDate;

    public static ReminderWrapper from(Reminder reminder) {
        ReminderWrapper w = new ReminderWrapper();
        w.id = reminder.getId();
        w.noteId = reminder.getNoteId();
        w.dateTime = reminder.getDateTime();
        w.repeatMode = reminder.getRepeatMode();
        w.repeatCount = reminder.getRepeatCount();
        w.repeatEndDate = reminder.getRepeatEndDate();
        return w;
    }

    public Reminder toReminder() {
        Reminder r = new Reminder();
        r.setId(TextUtil.isEmpty(id) ? UUID.randomUUID().toString() : id);
        r.setNoteId(noteId);
        r.setDateTime(dateTime);
        r.setRepeatMode(repeatMode);
        r.setRepeatCount(repeatCount);
        r.setRepeatEndDate(repeatEndDate);
        return r;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

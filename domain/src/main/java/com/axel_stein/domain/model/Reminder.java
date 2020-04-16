package com.axel_stein.domain.model;

import com.axel_stein.domain.utils.TextUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class Reminder {
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_DAY = 1;
    public static final int REPEAT_MODE_WEEK = 2;
    public static final int REPEAT_MODE_MONTH = 3;
    public static final int REPEAT_MODE_YEAR = 4;

    private String id;
    private String noteId;
    private DateTime dateTime;
    private int repeatMode;
    private int repeatCount;
    private LocalDate repeatEndDate;

    public boolean hasId() {
        return !TextUtil.isEmpty(id);
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

    public void moveDateTime() {
        switch (repeatMode) {
            case REPEAT_MODE_DAY:
                dateTime = dateTime.plusDays(repeatCount);
                break;

            case REPEAT_MODE_WEEK:
                dateTime = dateTime.plusWeeks(repeatCount);
                break;

            case REPEAT_MODE_MONTH:
                dateTime = dateTime.plusMonths(repeatCount);
                break;

            case REPEAT_MODE_YEAR:
                dateTime = dateTime.plusYears(repeatCount);
                break;
        }
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", noteId='" + noteId + '\'' +
                ", dateTime=" + dateTime +
                ", repeatMode=" + repeatMode +
                ", repeatCount=" + repeatCount +
                ", repeatEndDate=" + repeatEndDate +
                '}';
    }
}

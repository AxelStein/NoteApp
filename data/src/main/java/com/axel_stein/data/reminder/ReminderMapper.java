package com.axel_stein.data.reminder;

import com.axel_stein.domain.model.Reminder;

class ReminderMapper {

    static Reminder map(ReminderEntity entity) {
        if (entity == null) {
            return null;
        }
        Reminder reminder = new Reminder();
        reminder.setId(entity.getId());
        reminder.setNoteId(entity.getNoteId());
        reminder.setDateTime(entity.getDateTime());
        reminder.setRepeatMode(entity.getRepeatMode());
        reminder.setRepeatCount(entity.getRepeatCount());
        reminder.setRepeatEndDate(entity.getRepeatEndDate());
        return reminder;
    }

    static ReminderEntity map(Reminder reminder) {
        if (reminder == null) {
            return null;
        }
        ReminderEntity entity = new ReminderEntity();
        entity.setId(reminder.getId());
        entity.setNoteId(reminder.getNoteId());
        entity.setDateTime(reminder.getDateTime());
        entity.setRepeatMode(reminder.getRepeatMode());
        entity.setRepeatCount(reminder.getRepeatCount());
        entity.setRepeatEndDate(reminder.getRepeatEndDate());
        return entity;
    }
}

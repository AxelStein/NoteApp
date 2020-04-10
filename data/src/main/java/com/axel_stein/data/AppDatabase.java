package com.axel_stein.data;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.axel_stein.data.note.NoteDao;
import com.axel_stein.data.note.NoteEntity;
import com.axel_stein.data.notebook.NotebookDao;
import com.axel_stein.data.notebook.NotebookEntity;
import com.axel_stein.data.reminder.ReminderDao;
import com.axel_stein.data.reminder.ReminderEntity;

@Database(entities = {NotebookEntity.class, NoteEntity.class, ReminderEntity.class}, version = 4)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract NotebookDao notebookDao();

    public abstract NoteDao noteDao();

    public abstract ReminderDao reminderDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN checkList INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN checkListJson TEXT");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN hasReminder INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE notes ADD COLUMN reminderId TEXT");
            database.execSQL("CREATE TABLE IF NOT EXISTS reminders (id TEXT PRIMARY KEY NOT NULL, noteId TEXT, dateTime TEXT, repeatMode INTEGER NOT NULL, repeatCount INTEGER NOT NULL, repeatEndDate TEXT)");
        }
    };

}

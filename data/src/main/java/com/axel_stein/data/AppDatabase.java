package com.axel_stein.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import com.axel_stein.data.label.LabelDao;
import com.axel_stein.data.label.LabelEntity;
import com.axel_stein.data.note.NoteDao;
import com.axel_stein.data.note.NoteEntity;
import com.axel_stein.data.note_label_pair.NoteLabelPairDao;
import com.axel_stein.data.note_label_pair.NoteLabelPairEntity;
import com.axel_stein.data.notebook.NotebookDao;
import com.axel_stein.data.notebook.NotebookEntity;

@Database(entities = {LabelEntity.class, NotebookEntity.class, NoteLabelPairEntity.class, NoteEntity.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `notebooks` ADD `order` INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `notes` ADD `pinned` INTEGER NOT NULL DEFAULT 0");
        }
    };

    public abstract LabelDao labelDao();

    public abstract NoteLabelPairDao labelHelperDao();

    public abstract NotebookDao notebookDao();

    public abstract NoteDao noteDao();

}

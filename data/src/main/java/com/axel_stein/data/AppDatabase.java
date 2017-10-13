package com.axel_stein.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

import com.axel_stein.data.label.LabelDao;
import com.axel_stein.data.label.LabelEntity;
import com.axel_stein.data.note.NoteDao;
import com.axel_stein.data.note.NoteEntity;
import com.axel_stein.data.note_label_pair.NoteLabelPairDao;
import com.axel_stein.data.note_label_pair.NoteLabelPairEntity;
import com.axel_stein.data.notebook.NotebookDao;
import com.axel_stein.data.notebook.NotebookEntity;

@Database(entities = {LabelEntity.class, NotebookEntity.class, NoteLabelPairEntity.class, NoteEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Since we didn't alter the table, there's nothing else to do here.
        }
    };

    public abstract LabelDao labelDao();

    public abstract NoteLabelPairDao labelHelperDao();

    public abstract NotebookDao notebookDao();

    public abstract NoteDao noteDao();

}

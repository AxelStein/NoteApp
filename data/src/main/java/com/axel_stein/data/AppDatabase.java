package com.axel_stein.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.axel_stein.data.label.LabelDao;
import com.axel_stein.data.label.LabelEntity;
import com.axel_stein.data.note.NoteDao;
import com.axel_stein.data.note.NoteEntity;
import com.axel_stein.data.note_label_pair.NoteLabelPairDao;
import com.axel_stein.data.note_label_pair.NoteLabelPairEntity;
import com.axel_stein.data.notebook.NotebookDao;
import com.axel_stein.data.notebook.NotebookEntity;

@Database(entities = {LabelEntity.class, NotebookEntity.class, NoteLabelPairEntity.class, NoteEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract LabelDao labelDao();

    public abstract NoteLabelPairDao labelHelperDao();

    public abstract NotebookDao notebookDao();

    public abstract NoteDao noteDao();

}

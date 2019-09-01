package com.axel_stein.data;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.axel_stein.data.note.NoteDao;
import com.axel_stein.data.note.NoteEntity;
import com.axel_stein.data.notebook.NotebookDao;
import com.axel_stein.data.notebook.NotebookEntity;

@Database(entities = {NotebookEntity.class, NoteEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract NotebookDao notebookDao();

    public abstract NoteDao noteDao();

}

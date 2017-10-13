package com.axel_stein.labels;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.axel_stein.data.AppDatabase;
import com.axel_stein.data.note.NoteDao;
import com.axel_stein.data.note.NoteEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class NoteDaoTest {
    private AppDatabase mDatabase;
    private NoteDao mDao;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        mDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        mDao = mDatabase.noteDao();
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }

    @Test
    public void insert() throws Exception {
        NoteEntity entity = new NoteEntity();
        entity.setNotebook(1);
        entity.setTitle("Test");
        entity.setTrash(true);

        entity.setId(mDao.insert(entity));

        List<NoteEntity> list = mDao.query();
        assertTrue(list.size() > 0);
        assertTrue(list.get(0).isTrash());
    }

}

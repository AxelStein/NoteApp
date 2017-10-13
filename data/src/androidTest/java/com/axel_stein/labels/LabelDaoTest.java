package com.axel_stein.labels;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.axel_stein.data.AppDatabase;
import com.axel_stein.data.label.LabelDao;
import com.axel_stein.data.label.LabelEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LabelDaoTest {
    private AppDatabase mDatabase;
    private LabelDao mDao;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        mDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        mDao = mDatabase.labelDao();
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }

    @Test
    public void insert() throws Exception {
        LabelEntity entity = new LabelEntity();
        entity.setTitle("Test");

        entity.setId(mDao.insert(entity));

        assertTrue(entity.getId() > 0);
    }

    @Test
    public void delete() {
        // todo
    }

    @Test
    public void query() throws Exception {
        for (int i = 0; i < 5; i++) {
            LabelEntity entity = new LabelEntity();
            entity.setTitle("Item" + i);
            mDao.insert(entity);
        }

        List<LabelEntity> entities = mDao.query();
        for (LabelEntity entity : entities) {
            assertTrue(entity.getId() > 0);
            assertTrue(entity.getTitle() != null && entity.getTitle().length() > 0);
        }
    }

}

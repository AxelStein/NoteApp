package com.axel_stein.data.notebook;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.Nullable;

import java.util.List;

@Dao
public interface NotebookDao {

    @Insert
    long insert(NotebookEntity notebook);

    @Update
    void update(NotebookEntity notebook);

    @Delete
    void delete(NotebookEntity notebook);

    @Query("DELETE FROM notebooks")
    void deleteAll();

    @Query("SELECT * FROM notebooks WHERE id = :id LIMIT 1")
    @Nullable
    NotebookEntity get(long id);

    @Query("SELECT * FROM notebooks ORDER BY title")
    List<NotebookEntity> query();

}

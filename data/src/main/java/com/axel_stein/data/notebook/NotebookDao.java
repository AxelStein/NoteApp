package com.axel_stein.data.notebook;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NotebookDao {

    @Insert
    void insert(NotebookEntity notebook);

    @Update
    void update(NotebookEntity notebook);

    @Query("UPDATE notebooks SET title = :title WHERE id = :notebookId")
    void rename(String notebookId, String title);

    @Query("UPDATE notebooks SET views = :views WHERE id = :notebookId")
    void updateViews(String notebookId, long views);

    @Query("UPDATE notebooks SET `order` = :order WHERE id = :notebookId")
    void updateOrder(String notebookId, int order);

    @Query("UPDATE notebooks SET color = :color WHERE id = :notebookId")
    void updateColor(String notebookId, String color);

    @Query("SELECT * FROM notebooks WHERE id = :id LIMIT 1")
    @Nullable
    NotebookEntity get(String id);

    @Query("SELECT * FROM notebooks ORDER BY title")
    List<NotebookEntity> query();

    @Delete
    void delete(NotebookEntity notebook);

    @Query("DELETE FROM notebooks")
    void delete();

}

package com.axel_stein.data.label;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LabelDao {

    @Insert
    void insert(LabelEntity label);

    @Update
    void update(LabelEntity label);

    @Query("UPDATE labels SET title = :title WHERE id = :labelId")
    void rename(String labelId, String title);

    @Query("UPDATE labels SET views = :views WHERE id = :labelId")
    void updateViews(String labelId, long views);

    @Query("UPDATE labels SET `order` = :order WHERE id = :labelId")
    void updateOrder(String labelId, int order);

    @Query("SELECT * FROM labels WHERE id = :id LIMIT 1")
    @Nullable
    LabelEntity get(String id);

    @Query("SELECT * FROM labels ORDER BY title")
    List<LabelEntity> query();

    @Delete
    void delete(LabelEntity label);

    @Query("DELETE FROM labels")
    void delete();

}

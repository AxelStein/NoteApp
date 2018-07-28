package com.axel_stein.data.label;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.Nullable;

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

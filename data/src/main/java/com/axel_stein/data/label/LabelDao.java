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
    long insert(LabelEntity label);

    @Update
    void update(LabelEntity label);

    @Delete
    void delete(LabelEntity label);

    @Query("DELETE FROM labels")
    void deleteAll();

    @Query("SELECT * FROM labels WHERE id = :id LIMIT 1")
    @Nullable
    LabelEntity get(long id);

    @Query("SELECT * FROM labels ORDER BY title")
    List<LabelEntity> query();

}

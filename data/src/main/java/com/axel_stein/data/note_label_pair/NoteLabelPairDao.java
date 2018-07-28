package com.axel_stein.data.note_label_pair;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface NoteLabelPairDao {

    @Insert
    void insert(NoteLabelPairEntity entity);

    @Query("UPDATE note_label_pairs SET entity_trash = :trashed WHERE note_id = :noteId")
    void setTrashed(String noteId, boolean trashed);

    @Query("SELECT * FROM note_label_pairs")
    List<NoteLabelPairEntity> query();

    @Query("SELECT label_id FROM note_label_pairs WHERE note_id = :noteId")
    List<String> queryLabels(String noteId);

    @Query("SELECT COUNT(*) FROM note_label_pairs WHERE label_id = :labelId AND entity_trash = 0")
    long count(String labelId);

    @Query("DELETE FROM note_label_pairs WHERE label_id = :labelId")
    void deleteLabel(String labelId);

    @Query("DELETE FROM note_label_pairs WHERE note_id = :noteId") // fixme
    void deleteNote(String noteId);

    @Query("DELETE FROM note_label_pairs")
    void deleteAll();

}

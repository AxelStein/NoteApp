package com.axel_stein.data.note_label_pair;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface NoteLabelPairDao {

    @Insert
    void insert(NoteLabelPairEntity entity);

    @Query("DELETE FROM note_label_pairs WHERE label_id = :labelId")
    void deleteLabel(long labelId);

    @Query("DELETE FROM note_label_pairs WHERE note_id = :noteId") // fixme
    void deleteNote(String noteId);

    @Query("DELETE FROM note_label_pairs")
    void deleteAll();

    @Query("SELECT label_id FROM note_label_pairs WHERE note_id = :noteId")
    List<Long> queryLabelsOfNote(String noteId);

    @Query("SELECT * FROM note_label_pairs")
    List<NoteLabelPairEntity> query();

    @Query("SELECT COUNT(*) FROM note_label_pairs WHERE label_id = :labelId AND entity_trash = 0")
    long count(long labelId);

    @Query("UPDATE note_label_pairs SET entity_trash = 1 WHERE note_id = :noteId")
    void trash(String noteId);

    @Query("UPDATE note_label_pairs SET entity_trash = 0 WHERE note_id = :noteId")
    void restore(String noteId);

}

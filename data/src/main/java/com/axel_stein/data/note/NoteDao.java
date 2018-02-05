package com.axel_stein.data.note;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long insert(NoteEntity note);

    @Update
    void update(NoteEntity note);

    @Query("UPDATE notes SET notebook = :notebookId WHERE id = :noteId")
    void updateNotebook(long noteId, long notebookId);

    @Delete
    void delete(NoteEntity note);

    @Query("SELECT * FROM notes WHERE id = :id")
    NoteEntity get(long id);

    @Query("SELECT * FROM notes")
    List<NoteEntity> query();

    @Query("SELECT * FROM notes WHERE notebook = 0 AND trash = 0")
    List<NoteEntity> queryHome();

    @Query("SELECT * FROM notes WHERE notebook = :notebook AND trash = 0")
    List<NoteEntity> queryNotebook(long notebook);

    @Query("SELECT * FROM notes WHERE notebook = :notebook")
    List<NoteEntity> queryNotebookTrash(long notebook);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM notes JOIN note_label_pairs WHERE trash = 0 AND notes.id = note_id AND label_id = :label")
    List<NoteEntity> queryLabel(long label);

    @Query("SELECT * FROM notes WHERE trash = 0 AND (title LIKE :query OR content LIKE :query)")
    List<NoteEntity> search(String query);

    @Query("SELECT * FROM notes WHERE trash != 0")
    List<NoteEntity> queryTrash();

    @Query("UPDATE notes SET notebook = :notebook WHERE id IN (:notes)")
    void setNotebook(List<Long> notes, long notebook);

    @Query("UPDATE notes SET trash = 1, notebook = 0 WHERE id IN (:notes)")
    void trash(List<Long> notes);

    @Query("UPDATE notes SET trash = 0 WHERE id IN (:notes)")
    void restore(List<Long> notes);

    @Query("DELETE FROM notes")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM notes WHERE trash = 0 AND notebook = :notebook")
    long count(long notebook);

}

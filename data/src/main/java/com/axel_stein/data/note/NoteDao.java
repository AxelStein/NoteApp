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
    void updateNotebook(String noteId, long notebookId);

    @Delete
    void delete(NoteEntity note);

    @Query("SELECT * FROM notes WHERE id = :id")
    NoteEntity get(String id);

    @Query("SELECT * FROM notes")
    List<NoteEntity> query();

    @Query("SELECT * FROM notes WHERE notebook = 0 AND trash = 0 ORDER BY :orderBy")
    List<NoteEntity> queryHome(String orderBy);

    @Query("SELECT * FROM notes WHERE notebook = :notebook AND trash = 0 ORDER BY :orderBy")
    List<NoteEntity> queryNotebook(long notebook, String orderBy);

    @Query("SELECT * FROM notes WHERE notebook = :notebook ORDER BY :orderBy")
    List<NoteEntity> queryNotebookTrash(long notebook, String orderBy);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM notes JOIN note_label_pairs WHERE trash = 0 AND notes.id = note_id AND label_id = :label ORDER BY :orderBy")
    List<NoteEntity> queryLabel(long label, String orderBy);

    @Query("SELECT * FROM notes WHERE trash = 0 AND (title LIKE :query OR content LIKE :query)")
    List<NoteEntity> search(String query);

    @Query("SELECT * FROM notes WHERE trash != 0 ORDER BY :orderBy")
    List<NoteEntity> queryTrash(String orderBy);

    @Query("UPDATE notes SET notebook = :notebook WHERE id IN (:notes)")
    void setNotebook(List<String> notes, long notebook);

    @Query("UPDATE notes SET trash = 1 WHERE id IN (:notes)")
    void trash(List<String> notes);

    @Query("UPDATE notes SET trash = 0 WHERE id IN (:notes)")
    void restore(List<String> notes);

    @Query("UPDATE notes SET pinned = 1 WHERE id IN (:notes)")
    void pin(List<String> notes);

    @Query("UPDATE notes SET pinned = 0 WHERE id IN (:notes)")
    void unpin(List<String> notes);

    @Query("DELETE FROM notes WHERE notebook = :notebook AND trash = 0")
    void deleteNotebook(long notebook);

    @Query("UPDATE notes SET notebook = 0 WHERE notebook = :notebook")
    void setHome(long notebook);

    @Query("DELETE FROM notes")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM notes WHERE trash = 0 AND notebook = :notebook")
    long count(long notebook);

}

package com.axel_stein.data.note;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;
import android.arch.persistence.room.Update;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void insert(NoteEntity note);

    /* Update methods */

    @Update
    void update(NoteEntity note);

    @Query("UPDATE notes SET notebookId = :notebookId WHERE id = :noteId")
    void setNotebook(String noteId, String notebookId);

    @Query("UPDATE notes SET notebookId = :notebookId WHERE id IN (:noteIds)")
    void setNotebook(List<String> noteIds, String notebookId);

    @Query("UPDATE notes SET notebookId = NULL WHERE notebookId = :notebook")
    void setInbox(String notebook);

    @Query("UPDATE notes SET pinned = :pinned WHERE id = :noteId")
    void setPinned(String noteId, boolean pinned);

    @Query("UPDATE notes SET pinned = :pinned WHERE id IN (:noteIds)")
    void setPinned(List<String> noteIds, boolean pinned);

    @Query("UPDATE notes SET starred = :starred WHERE id = :noteId")
    void setStarred(String noteId, boolean starred);

    @Query("UPDATE notes SET starred = :starred WHERE id IN (:noteIds)")
    void setStarred(List<String> noteIds, boolean starred);

    @Query("UPDATE notes SET trashed = :trashed, trashedDate = :date WHERE id = :noteId")
    void setTrashed(String noteId, boolean trashed, DateTime date);

    @Query("UPDATE notes SET views = :views WHERE id = :noteId")
    void updateViews(String noteId, long views);

    /* Delete methods */

    @Delete
    void delete(NoteEntity note);

    @Query("DELETE FROM notes WHERE notebookId = :notebookId AND trashed = 0")
    void deleteNotebook(String notebookId);

    @Query("DELETE FROM notes")
    void deleteAll();

    /* Query methods */

    @Query("SELECT * FROM notes WHERE id = :id")
    NoteEntity get(String id);

    @Query("SELECT COUNT(*) FROM notes WHERE trashed = 0 AND notebookId = :notebookId")
    long count(String notebookId);

    @Query("SELECT * FROM notes WHERE trashed = 0")
    List<NoteEntity> queryAll();

    @Query("SELECT * FROM notes")
    List<NoteEntity> queryAllTrashed();

    @Query("SELECT * FROM notes WHERE notebookId IS NULL AND trashed = 0")
    List<NoteEntity> queryInbox();

    @Query("SELECT * FROM notes WHERE starred != 0 AND trashed = 0")
    List<NoteEntity> queryStarred();

    @Query("SELECT * FROM notes WHERE trashed != 0")
    List<NoteEntity> queryTrashed();

    @Query("SELECT * FROM notes WHERE notebookId = :notebook AND trashed = 0")
    List<NoteEntity> queryNotebook(String notebook);

    @Query("SELECT * FROM notes WHERE notebookId = :notebook")
    List<NoteEntity> queryNotebookTrashed(String notebook);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM notes JOIN note_label_pairs WHERE trashed = 0 AND notes.id = note_id AND label_id = :labelId")
    List<NoteEntity> queryLabel(String labelId);

    @Query("SELECT * FROM notes WHERE trashed = 0 AND (title LIKE :query OR content LIKE :query)")
    List<NoteEntity> search(String query);

}

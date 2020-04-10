package com.axel_stein.data.note;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("UPDATE notes SET title = :title WHERE id = :noteId")
    void updateTitle(String noteId, String title);

    @Query("UPDATE notes SET content = :content WHERE id = :noteId")
    void updateContent(String noteId, String content);

    @Query("UPDATE notes SET modifiedDate = :dateTime WHERE id = :noteId")
    void updateModifiedDate(String noteId, DateTime dateTime);

    @Query("UPDATE notes SET checkList = :isCheckList WHERE id = :noteId")
    void updateIsCheckList(String noteId, boolean isCheckList);

    @Query("UPDATE notes SET checkListJson = :checkListJson WHERE id = :noteId")
    void updateCheckListJson(String noteId, String checkListJson);

    @Query("UPDATE notes SET reminderId = :reminderId, hasReminder = 1 WHERE id = :noteId")
    void setReminder(String noteId, String reminderId);

    @Query("UPDATE notes SET reminderId = '', hasReminder = 0 WHERE reminderId = :reminderId")
    void deleteReminder(String reminderId);

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

    @Query("SELECT reminderId FROM notes WHERE id = :noteId")
    String getReminderId(String noteId);

    @Query("SELECT * FROM notes")
    List<NoteEntity> queryAll();

    @Query("SELECT * FROM notes WHERE (notebookId IS NULL OR notebookId = '') AND trashed = 0")
    List<NoteEntity> queryInbox();

    @Query("SELECT * FROM notes WHERE starred != 0 AND trashed = 0")
    List<NoteEntity> queryStarred();

    @Query("SELECT * FROM notes WHERE hasReminder != 0 AND trashed = 0")
    List<NoteEntity> queryReminders();

    @Query("SELECT * FROM notes WHERE trashed != 0")
    List<NoteEntity> queryTrashed();

    @Query("SELECT * FROM notes WHERE notebookId = :notebook AND trashed = 0")
    List<NoteEntity> queryNotebook(String notebook);

    @Query("SELECT * FROM notes WHERE trashed = 0 AND (title LIKE :query OR content LIKE :query)")
    List<NoteEntity> search(String query);

}

package com.axel_stein.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;

import org.joda.time.DateTime;

import java.util.List;

public interface NoteRepository {

    void insert(@NonNull Note note);

    void update(@NonNull Note note);

    void setNotebook(String noteId, String notebookId);

    void setNotebook(@NonNull List<Note> notes, String notebookId);

    void setInbox(@NonNull Notebook notebook);

    void setPinned(@NonNull Note note, boolean pinned);

    void setPinned(@NonNull List<Note> notes, boolean pinned);

    void setStarred(@NonNull Note note, boolean starred);

    void setStarred(@NonNull List<Note> notes, boolean starred);

    void setTrashed(@NonNull Note note, boolean trashed);

    void updateViews(@NonNull Note note, long views);

    void updateTitle(String noteId, String title);

    void updateContent(String noteId, String content);

    void updateModifiedDate(String noteId, DateTime dateTime);

    void updateIsCheckList(String noteId, boolean isCheckList);

    void updateCheckListJson(String noteId, String checkListJson);

    void delete(@NonNull Note note);

    void deleteNotebook(@NonNull Notebook notebook);

    void deleteAll();

    @Nullable
    Note get(String id);

    @NonNull
    List<Note> queryAll();

    @NonNull
    List<Note> queryInbox();

    @NonNull
    List<Note> queryStarred();

    @NonNull
    List<Note> queryTrashed();

    @NonNull
    List<Note> queryNotebook(@NonNull String notebookId);

    @NonNull
    List<Note> search(@NonNull String query);

}

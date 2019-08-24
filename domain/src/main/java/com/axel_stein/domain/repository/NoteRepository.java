package com.axel_stein.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;

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

    void delete(@NonNull Note note);

    void deleteNotebook(@NonNull Notebook notebook);

    void deleteAll();

    @Nullable
    Note get(String id);

    long count(@NonNull Notebook notebook);

    @NonNull
    List<Note> queryAll();

    @NonNull
    List<Note> queryAllTrashed();

    @NonNull
    List<Note> queryInbox();

    @NonNull
    List<Note> queryStarred();

    @NonNull
    List<Note> queryTrashed();

    @NonNull
    List<Note> queryNotebook(@NonNull Notebook notebook);

    @NonNull
    List<Note> queryNotebook(@NonNull String notebookId);

    List<Note> queryNotebookTrashed(@NonNull Notebook notebook);

    @NonNull
    List<Note> queryLabel(@NonNull Label label);

    @NonNull
    List<Note> search(@NonNull String query);

    @NonNull
    List<Note> searchByTitle(@NonNull String query);

    @NonNull
    List<Note> searchByContent(@NonNull String query);

}

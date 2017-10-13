package com.axel_stein.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;

import java.util.List;

public interface NoteRepository {

    /**
     * Inserts note into repository
     *
     * @param note to insert
     * @return id of the created note
     */
    long insert(@NonNull Note note);

    void update(@NonNull Note note);

    void delete(@NonNull Note note);

    @Nullable
    Note get(long id);

    long count(@NonNull Notebook notebook);

    void setNotebook(@NonNull List<Note> notes, long notebookId);

    void trash(@NonNull List<Note> notes);

    void restore(@NonNull List<Note> notes);

    @NonNull
    List<Note> query();

    @NonNull
    List<Note> query(@NonNull Notebook notebook);

    List<Note> query(@NonNull Notebook notebook, boolean includeTrash);

    @NonNull
    List<Note> query(@NonNull Label label);

    /**
     * @param query not null and not empty
     */
    @NonNull
    List<Note> search(@NonNull String query);

    @NonNull
    List<Note> queryTrash();

    void deleteAll();

}

package com.axel_stein.data.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;
import java.util.UUID;

import static com.axel_stein.data.note.NoteMapper.map;
import static com.axel_stein.data.note.NoteMapper.mapIds;

public class SqlNoteRepository implements NoteRepository {

    private NoteDao mDao;

    public SqlNoteRepository(@NonNull NoteDao dao) {
        mDao = dao;
    }

    @Override
    public void insert(@NonNull Note note) {
        if (!note.hasId()) {
            note.setId(UUID.randomUUID().toString());
        }
        mDao.insert(map(note));
    }

    /* Update methods */

    @Override
    public void update(@NonNull Note note) {
        mDao.update(map(note));
    }

    @Override
    public void setNotebook(String noteId, String notebookId) {
        mDao.setNotebook(noteId, notebookId);
    }

    @Override
    public void setNotebook(@NonNull List<Note> notes, String notebookId) {
        mDao.setNotebook(mapIds(notes), notebookId);
    }

    @Override
    public void setInbox(@NonNull Notebook notebook) {
        mDao.setInbox(notebook.getId());
    }

    @Override
    public void setPinned(@NonNull Note note, boolean pinned) {
        mDao.setPinned(note.getId(), pinned);
    }

    @Override
    public void setPinned(@NonNull List<Note> notes, boolean pinned) {
        mDao.setPinned(mapIds(notes), pinned);
    }

    @Override
    public void setStarred(@NonNull Note note, boolean starred) {
        mDao.setStarred(note.getId(), starred);
    }

    @Override
    public void setStarred(@NonNull List<Note> notes, boolean starred) {
        mDao.setStarred(mapIds(notes), starred);
    }

    @Override
    public void setTrashed(@NonNull Note note, boolean trashed) {
        mDao.setTrashed(note.getId(), trashed, note.getTrashedDate());
    }

    @Override
    public void updateViews(@NonNull Note note, long views) {
        mDao.updateViews(note.getId(), views);
    }

    /* Delete methods */

    @Override
    public void delete(@NonNull Note note) {
        mDao.delete(map(note));
    }

    @Override
    public void deleteNotebook(@NonNull Notebook notebook) {
        mDao.deleteNotebook(notebook.getId());
    }

    @Override
    public void deleteAll() {
        mDao.deleteAll();
    }

    /* Query methods */

    @Nullable
    @Override
    public Note get(String id) {
        return map(mDao.get(id));
    }

    @NonNull
    @Override
    public List<Note> queryAll() {
        return map(mDao.queryAll());
    }

    @NonNull
    @Override
    public List<Note> queryInbox() {
        return map(mDao.queryInbox());
    }

    @NonNull
    @Override
    public List<Note> queryStarred() {
        return map(mDao.queryStarred());
    }

    @NonNull
    @Override
    public List<Note> queryTrashed() {
        return map(mDao.queryTrashed());
    }

    @NonNull
    @Override
    public List<Note> queryNotebook(@NonNull String notebookId) {
        return map(mDao.queryNotebook(notebookId));
    }

    @NonNull
    @Override
    public List<Note> search(@NonNull String query) {
        query = "%" + query + "%";
        return map(mDao.search(query));
    }

}

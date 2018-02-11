package com.axel_stein.data.note;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;

import static com.axel_stein.data.note.NoteMapper.map;
import static com.axel_stein.data.note.NoteMapper.mapIds;

public class SqlNoteRepository implements NoteRepository {

    private NoteDao mDao;

    public SqlNoteRepository(NoteDao dao) {
        mDao = dao;
    }

    @Override
    public long insert(@NonNull Note note) {
        return mDao.insert(map(note));
    }

    @Override
    public void update(@NonNull Note note) {
        mDao.update(map(note));
    }

    @Override
    public void updateNotebook(long noteId, long notebookId) {
        mDao.updateNotebook(noteId, notebookId);
    }

    @Override
    public void delete(@NonNull Note note) {
        mDao.delete(map(note));
    }

    @Nullable
    @Override
    public Note get(long id) {
        return map(mDao.get(id));
    }

    @Override
    public long count(@NonNull Notebook notebook) {
        return mDao.count(notebook.getId());
    }

    @Override
    public void setNotebook(@NonNull List<Note> notes, long notebookId) {
        mDao.setNotebook(mapIds(notes), notebookId);
    }

    @Override
    public void trash(@NonNull List<Note> notes) {
        mDao.trash(mapIds(notes));
    }

    @Override
    public void restore(@NonNull List<Note> notes) {
        mDao.restore(mapIds(notes));
    }

    @Override
    public void pin(@NonNull List<Note> notes) {
        mDao.pin(mapIds(notes));
    }

    @Override
    public void unpin(@NonNull List<Note> notes) {
        mDao.unpin(mapIds(notes));
    }

    @Override
    public void deleteNotebook(@NonNull Notebook notebook) {
        mDao.deleteNotebook(notebook.getId());
    }

    @Override
    public void setHome(@NonNull Notebook notebook) {
        mDao.setHome(notebook.getId());
    }

    @NonNull
    @Override
    public List<Note> query() {
        return map(mDao.query());
    }

    @NonNull
    @Override
    public List<Note> queryHome() {
        return map(mDao.queryHome());
    }

    @NonNull
    @Override
    public List<Note> query(@NonNull Notebook notebook) {
        return map(mDao.queryNotebook(notebook.getId()));
    }

    @Override
    public List<Note> query(@NonNull Notebook notebook, boolean includeTrash) {
        if (!includeTrash) {
            return query(notebook);
        }
        return map(mDao.queryNotebookTrash(notebook.getId()));
    }

    @NonNull
    @Override
    public List<Note> query(@NonNull Label label) {
        return map(mDao.queryLabel(label.getId()));
    }

    @NonNull
    @Override
    public List<Note> search(@NonNull String query) {
        query = "%" + query + "%";
        return map(mDao.search(query));
    }

    @NonNull
    @Override
    public List<Note> queryTrash() {
        return map(mDao.queryTrash());
    }

    @Override
    public void deleteAll() {
        mDao.deleteAll();
    }

}

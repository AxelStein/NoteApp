package com.axel_stein.data.note;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;
import java.util.UUID;

import static com.axel_stein.data.note.NoteMapper.map;
import static com.axel_stein.data.note.NoteMapper.mapIds;

public class SqlNoteRepository implements NoteRepository {

    private NoteDao mDao;

    private AppSettingsRepository mSettings;

    public SqlNoteRepository(NoteDao dao, AppSettingsRepository settings) {
        mDao = dao;
        mSettings = settings;
    }

    private String getOrderBy() {
        NoteOrder order = mSettings.getNotesOrder();
        switch (order) {
            case TITLE:
                return "title";
            case RELEVANCE:
                return "relevance desc";
            case CREATED_NEWEST:
                return "datetime(created) desc";
            case CREATED_OLDEST:
                return "datetime(created)";
            case MODIFIED_NEWEST:
                return "datetime(modified) desc";
            case MODIFIED_OLDEST:
                return "datetime(modified)";
        }
        return "title";
    }

    @Override
    public String insert(@NonNull Note note) {
        note.setId(UUID.randomUUID().toString());
        mDao.insert(map(note));
        return note.getId();
    }

    @Override
    public void update(@NonNull Note note) {
        mDao.update(map(note));
    }

    @Override
    public void updateNotebook(String noteId, long notebookId) {
        mDao.updateNotebook(noteId, notebookId);
    }

    @Override
    public void delete(@NonNull Note note) {
        mDao.delete(map(note));
    }

    @Nullable
    @Override
    public Note get(String id) {
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
        return map(mDao.queryHome(getOrderBy()));
    }

    @NonNull
    @Override
    public List<Note> query(@NonNull Notebook notebook) {
        return map(mDao.queryNotebook(notebook.getId(), getOrderBy()));
    }

    @Override
    public List<Note> query(@NonNull Notebook notebook, boolean includeTrash) {
        if (!includeTrash) {
            return query(notebook);
        }
        return map(mDao.queryNotebookTrash(notebook.getId(), getOrderBy()));
    }

    @NonNull
    @Override
    public List<Note> query(@NonNull Label label) {
        return map(mDao.queryLabel(label.getId(), getOrderBy()));
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
        return map(mDao.queryTrash(getOrderBy()));
    }

    @Override
    public void deleteAll() {
        mDao.deleteAll();
    }

}

package com.axel_stein.data.notebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NotebookRepository;

import java.util.List;
import java.util.UUID;

public class SqlNotebookRepository implements NotebookRepository {

    @NonNull
    private final NotebookDao mDao;

    public SqlNotebookRepository(@NonNull NotebookDao dao) {
        mDao = dao;
    }

    @Override
    public void insert(@NonNull Notebook notebook) {
        if (!notebook.hasId()) {
            notebook.setId(UUID.randomUUID().toString());
        }
        mDao.insert(NotebookMapper.map(notebook));
    }

    @Override
    public void update(@NonNull Notebook notebook) {
        mDao.update(NotebookMapper.map(notebook));
    }

    @Override
    public void rename(@NonNull Notebook notebook, String title) {
        mDao.rename(notebook.getId(), title);
    }

    @Override
    @Nullable
    public Notebook get(String id) {
        return NotebookMapper.map(mDao.get(id));
    }

    @NonNull
    @Override
    public List<Notebook> query() {
        return NotebookMapper.map(mDao.query());
    }

    @Override
    public void delete(@NonNull Notebook notebook) {
        mDao.delete(NotebookMapper.map(notebook));
    }

    @Override
    public void deleteAll() {
        mDao.deleteAll();
    }

}

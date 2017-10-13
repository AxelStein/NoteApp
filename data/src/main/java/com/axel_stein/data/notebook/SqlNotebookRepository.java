package com.axel_stein.data.notebook;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NotebookRepository;

import java.util.List;

public class SqlNotebookRepository implements NotebookRepository {

    private NotebookDao mDao;

    public SqlNotebookRepository(NotebookDao dao) {
        mDao = dao;
    }

    @Override
    public long insert(@NonNull Notebook notebook) {
        return mDao.insert(NotebookMapper.map(notebook));
    }

    @Override
    public void update(@NonNull Notebook notebook) {
        mDao.update(NotebookMapper.map(notebook));
    }

    @Override
    public void delete(@NonNull Notebook notebook) {
        mDao.delete(NotebookMapper.map(notebook));
    }

    @Override
    @Nullable
    public Notebook get(long id) {
        return NotebookMapper.map(mDao.get(id));
    }

    @NonNull
    @Override
    public List<Notebook> query() {
        return NotebookMapper.map(mDao.query());
    }

    @Override
    public void deleteAll() {
        mDao.deleteAll();
    }

}

package com.axel_stein.data.label;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.repository.LabelRepository;

import java.util.List;
import java.util.UUID;

public class SqlLabelRepository implements LabelRepository {

    @NonNull
    private LabelDao mDao;

    public SqlLabelRepository(@NonNull LabelDao dao) {
        mDao = dao;
    }

    @Override
    public void insert(@NonNull Label label) {
        if (!label.hasId()) {
            label.setId(UUID.randomUUID().toString());
        }
        mDao.insert(LabelMapper.map(label));
    }

    @Override
    public void update(@NonNull Label label) {
        mDao.update(LabelMapper.map(label));
    }

    @Override
    public void rename(@NonNull Label label, String title) {
        mDao.rename(label.getId(), title);
    }

    @Override
    public void updateViews(@NonNull Label label, long views) {
        mDao.updateViews(label.getId(), views);
    }

    @Override
    public void updateOrder(@NonNull Label label, int order) {
        mDao.updateOrder(label.getId(), order);
    }

    @Override
    @Nullable
    public Label get(String id) {
        return LabelMapper.map(mDao.get(id));
    }

    @NonNull
    @Override
    public List<Label> query() {
        return LabelMapper.map(mDao.query());
    }

    @Override
    public void delete(@NonNull Label label) {
        mDao.delete(LabelMapper.map(label));
    }

    @Override
    public void delete() {
        mDao.delete();
    }

}

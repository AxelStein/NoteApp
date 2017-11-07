package com.axel_stein.data.label;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.repository.LabelRepository;

import java.util.List;

import static com.axel_stein.data.ObjectUtil.checkNotNull;

public class SqlLabelRepository implements LabelRepository {

    @NonNull
    private LabelDao mDao;

    public SqlLabelRepository(@NonNull LabelDao dao) {
        mDao = checkNotNull(dao, "dao is null");
    }

    @Override
    public long insert(@NonNull Label label) {
        return mDao.insert(LabelMapper.map(label));
    }

    @Override
    public void update(@NonNull Label label) {
        mDao.update(LabelMapper.map(label));
    }

    @Override
    public void delete(@NonNull Label label) {
        mDao.delete(LabelMapper.map(label));
    }

    @Override
    @Nullable
    public Label get(long id) {
        return LabelMapper.map(mDao.get(id));
    }

    @NonNull
    @Override
    public List<Label> query() {
        return LabelMapper.map(mDao.query());
    }

    @Override
    public void deleteAll() {
        mDao.deleteAll();
    }

}

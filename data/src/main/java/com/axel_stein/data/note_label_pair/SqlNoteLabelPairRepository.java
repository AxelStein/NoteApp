package com.axel_stein.data.note_label_pair;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.repository.NoteLabelPairRepository;

import java.util.List;

import static com.axel_stein.data.ObjectUtil.checkNotNull;
import static com.axel_stein.data.note_label_pair.NoteLabelPairMapper.map;

public class SqlNoteLabelPairRepository implements NoteLabelPairRepository {

    @NonNull
    private NoteLabelPairDao mDao;

    public SqlNoteLabelPairRepository(@NonNull NoteLabelPairDao dao) {
        mDao = checkNotNull(dao, "dao is null");
    }

    @Override
    public void insert(NoteLabelPair pair) {
        mDao.insert(map(pair));
    }

    @Override
    public void setTrashed(@NonNull Note note, boolean trashed) {
        mDao.setTrashed(note.getId(), trashed);
    }

    @Override
    public List<NoteLabelPair> query() {
        return map(mDao.query());
    }

    @Override
    public List<String> queryLabels(@NonNull Note note) {
        return mDao.queryLabels(note.getId());
    }

    @Override
    public long count(@NonNull Label label) {
        return mDao.count(label.getId());
    }

    @Override
    public void delete(@NonNull Label label) {
        mDao.deleteLabel(label.getId());
    }

    @Override
    public void delete(@NonNull Note note) {
        mDao.deleteNote(note.getId());
    }

    @Override
    public void deleteAll() {
        mDao.deleteAll();
    }

}

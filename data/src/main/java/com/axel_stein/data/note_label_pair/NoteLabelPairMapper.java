package com.axel_stein.data.note_label_pair;

import androidx.annotation.Nullable;

import com.axel_stein.domain.model.NoteLabelPair;

import java.util.ArrayList;
import java.util.List;

public class NoteLabelPairMapper {

    static NoteLabelPair map(@Nullable NoteLabelPairEntity entity) {
        if (entity == null) {
            return null;
        }
        return new NoteLabelPair(entity.getNoteId(), entity.getLabelId(), entity.isTrash());
    }

    static NoteLabelPairEntity map(@Nullable NoteLabelPair pair) {
        if (pair == null) {
            return null;
        }
        return new NoteLabelPairEntity(pair.getNoteId(), pair.getLabelId(), pair.isTrash());
    }

    static List<NoteLabelPair> map(@Nullable List<NoteLabelPairEntity> entities) {
        if (entities == null) {
            return null;
        }
        List<NoteLabelPair> pairs = new ArrayList<>();
        for (NoteLabelPairEntity e : entities) {
            pairs.add(map(e));
        }
        return pairs;
    }

}

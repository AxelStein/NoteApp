package com.axel_stein.data.label;

import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Label;

import java.util.ArrayList;
import java.util.List;

class LabelMapper {

    static Label map(@Nullable LabelEntity entity) {
        if (entity == null) {
            return null;
        }
        Label label = new Label();
        label.setId(entity.getId());
        label.setTitle(entity.getTitle());
        return label;
    }

    static LabelEntity map(@Nullable Label label) {
        if (label == null) {
            return null;
        }
        LabelEntity entity = new LabelEntity();
        entity.setId(label.getId());
        entity.setTitle(label.getTitle());
        return entity;
    }

    static List<Label> map(@Nullable List<LabelEntity> entities) {
        if (entities == null) {
            return null;
        }
        List<Label> labels = new ArrayList<>();
        for (LabelEntity e : entities) {
            labels.add(map(e));
        }
        return labels;
    }

}

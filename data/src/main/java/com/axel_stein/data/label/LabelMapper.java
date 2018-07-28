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
        label.setOrder(entity.getOrder());
        label.setViews(entity.getViews());
        label.setCreatedDate(entity.getCreatedDate());
        label.setModifiedDate(entity.getModifiedDate());
        label.setDriveId(entity.getDriveId());
        return label;
    }

    static LabelEntity map(@Nullable Label label) {
        if (label == null) {
            return null;
        }
        LabelEntity entity = new LabelEntity();
        entity.setId(label.getId());
        entity.setTitle(label.getTitle());
        entity.setOrder(label.getOrder());
        entity.setViews(label.getViews());
        entity.setCreatedDate(label.getCreatedDate());
        entity.setModifiedDate(label.getModifiedDate());
        entity.setDriveId(label.getDriveId());
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

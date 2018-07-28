package com.axel_stein.data.notebook;

import com.axel_stein.domain.model.Notebook;

import java.util.ArrayList;
import java.util.List;

class NotebookMapper {

    static Notebook map(NotebookEntity entity) {
        if (entity == null) {
            return null;
        }
        Notebook notebook = new Notebook();
        notebook.setId(entity.getId());
        notebook.setTitle(entity.getTitle());
        notebook.setOrder(entity.getOrder());
        notebook.setViews(entity.getViews());
        notebook.setColor(entity.getColor());
        notebook.setCreatedDate(entity.getCreatedDate());
        notebook.setModifiedDate(entity.getModifiedDate());
        notebook.setDriveId(entity.getDriveId());
        return notebook;
    }

    static NotebookEntity map(Notebook notebook) {
        if (notebook == null) {
            return null;
        }
        NotebookEntity entity = new NotebookEntity();
        entity.setId(notebook.getId());
        entity.setTitle(notebook.getTitle());
        entity.setOrder(notebook.getOrder());
        entity.setViews(notebook.getViews());
        entity.setColor(notebook.getColor());
        entity.setCreatedDate(notebook.getCreatedDate());
        entity.setModifiedDate(notebook.getModifiedDate());
        entity.setDriveId(notebook.getDriveId());
        return entity;
    }

    static List<Notebook> map(List<NotebookEntity> entities) {
        if (entities == null) {
            return null;
        }
        List<Notebook> notebooks = new ArrayList<>();
        for (NotebookEntity e : entities) {
            notebooks.add(map(e));
        }
        return notebooks;
    }

}

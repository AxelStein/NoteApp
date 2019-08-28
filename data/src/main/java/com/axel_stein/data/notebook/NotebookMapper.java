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
        return notebook;
    }

    static NotebookEntity map(Notebook notebook) {
        if (notebook == null) {
            return null;
        }
        NotebookEntity entity = new NotebookEntity();
        entity.setId(notebook.getId());
        entity.setTitle(notebook.getTitle());
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

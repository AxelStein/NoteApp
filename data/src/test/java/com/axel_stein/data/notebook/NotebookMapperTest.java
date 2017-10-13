package com.axel_stein.data.notebook;

import com.axel_stein.domain.model.Notebook;

import org.junit.Test;

import static com.axel_stein.data.notebook.NotebookMapper.map;
import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotebookMapperTest {

    @Test
    public void mapEntity() {
        NotebookEntity entity = new NotebookEntity();
        assertFalse(isValid(map(entity)));

        entity = new NotebookEntity();
        entity.setId(1);
        assertFalse(isValid(map(entity)));

        entity = new NotebookEntity();
        entity.setTitle("Test");
        assertFalse(isValid(map(entity)));

        entity = new NotebookEntity();
        entity.setId(1);
        entity.setTitle("Test");

        assertTrue(isValid(map(entity)));
    }

    @Test
    public void mapNotebook() {
        Notebook notebook = new Notebook();
        assertFalse(isValidEntity(map(notebook)));

        notebook = new Notebook();
        notebook.setId(1);
        assertFalse(isValidEntity(map(notebook)));

        notebook = new Notebook();
        notebook.setTitle("Test");
        assertFalse(isValidEntity(map(notebook)));

        notebook = new Notebook();
        notebook.setId(1);
        notebook.setTitle("Test");
        assertTrue(isValidEntity(map(notebook)));
    }

    private boolean isValidEntity(NotebookEntity entity) {
        return entity != null && entity.getId() > 0 && !isEmpty(entity.getTitle());
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
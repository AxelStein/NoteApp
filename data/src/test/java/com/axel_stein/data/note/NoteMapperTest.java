package com.axel_stein.data.note;

import com.axel_stein.domain.model.Note;

import org.junit.Test;

import static com.axel_stein.data.note.NoteMapper.map;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NoteMapperTest {

    @Test
    public void mapEntity() {
        NoteEntity entity = new NoteEntity();
        assertFalse(isValid(map(entity)));

        entity = new NoteEntity();
        entity.setId(1);
        assertFalse(isValid(map(entity)));

        entity = new NoteEntity();
        entity.setTitle("Test");
        assertFalse(isValid(map(entity)));

        entity = new NoteEntity();
        entity.setId(1);
        entity.setTitle("Test");
        assertFalse(isValid(map(entity)));

        entity = new NoteEntity();
        entity.setId(1);
        entity.setTitle("Test");
        entity.setNotebookId(1);
        assertTrue(isValid(map(entity)));
    }

    @Test
    public void mapNote() {
        Note note = new Note();
        assertFalse(isValidEntity(map(note)));

        note = new Note();
        note.setId(1);
        assertFalse(isValidEntity(map(note)));

        note = new Note();
        note.setTitle("Test");
        assertFalse(isValidEntity(map(note)));

        note = new Note();
        note.setId(1);
        note.setTitle("Test");
        assertFalse(isValidEntity(map(note)));

        note = new Note();
        note.setId(1);
        note.setTitle("Test");
        note.setNotebookId(1);
        assertTrue(isValidEntity(map(note)));
    }

    private boolean isValidEntity(NoteEntity entity) {
        return entity != null && entity.getId() > 0 && entity.getNotebookId() > 0
                && !isEmpty(entity.getTitle());
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.Note;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NoteValidatorTest {

    @Test
    public void validateBeforeInsert() throws Exception {
        Note note = null;
        assertFalse(NoteValidator.validateBeforeInsert(note));

        note = new Note();
        assertFalse(NoteValidator.validateBeforeInsert(note));

        note = new Note();
        note.setNotebookId(1);
        assertFalse(NoteValidator.validateBeforeInsert(note));

        note = new Note();
        note.setTitle("Test");
        note.setNotebookId(1);
        assertTrue(NoteValidator.validateBeforeInsert(note));

        note = new Note();
        note.setContent("Test");
        note.setNotebookId(1);
        assertTrue(NoteValidator.validateBeforeInsert(note));
    }

    @Test
    public void validateBeforeUpdate() throws Exception {
        Note note = null;
        assertFalse(NoteValidator.validateBeforeUpdate(note));

        note = new Note();
        assertFalse(NoteValidator.validateBeforeUpdate(note));

        note = new Note();
        note.setNotebookId(1);
        assertFalse(NoteValidator.validateBeforeUpdate(note));

        note = new Note();
        note.setTitle("Test");
        note.setNotebookId(1);
        assertFalse(NoteValidator.validateBeforeUpdate(note));

        note = new Note();
        note.setId(2);
        note.setTitle("Test");
        note.setNotebookId(1);
        assertTrue(NoteValidator.validateBeforeUpdate(note));

        note = new Note();
        note.setId(2);
        note.setNotebookId(1);
        assertFalse(NoteValidator.validateBeforeUpdate(note));

        note = new Note();
        note.setId(2);
        note.setNotebookId(1);
        note.setContent("Test");
        assertTrue(NoteValidator.validateBeforeUpdate(note));
    }

    @Test
    public void isValid() throws Exception {
        Note note = null;
        assertFalse(NoteValidator.isValid(note));

        note = new Note();
        assertFalse(NoteValidator.isValid(note));

        note = new Note();
        note.setId(1);
        assertFalse(NoteValidator.isValid(note));

        note = new Note();
        note.setTitle("Test");
        assertFalse(NoteValidator.isValid(note));

        note = new Note();
        note.setNotebookId(2);
        assertFalse(NoteValidator.isValid(note));

        note = new Note();
        note.setTitle("Test");
        note.setNotebookId(2);
        assertFalse(NoteValidator.isValid(note));

        note = new Note();
        note.setId(1);
        note.setTitle("Test");
        assertFalse(NoteValidator.isValid(note));

        note = new Note();
        note.setId(1);
        note.setNotebookId(2);
        assertFalse(NoteValidator.isValid(note));

        note = new Note();
        note.setId(1);
        note.setTitle("Test");
        note.setNotebookId(2);
        assertTrue(NoteValidator.isValid(note));
    }

    @Test
    public void isValid_list() throws Exception {
        List<Note> list = null;
        assertFalse(NoteValidator.isValid(list));

        Note note = new Note();
        note.setId(1);
        note.setTitle("Test");
        note.setNotebookId(2);

        list = new ArrayList<>();
        list.add(note);

        assertTrue(NoteValidator.isValid(list));

        list = new ArrayList<>();
        list.add(note);
        list.add(new Note());

        assertFalse(NoteValidator.isValid(list));
    }

}
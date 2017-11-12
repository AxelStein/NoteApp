package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.Note;

import java.util.List;

import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class NoteValidator {

    public static boolean validateBeforeInsert(Note note) {
        if (note == null || note.getNotebook() <= 0) {
            return false;
        }

        if (isEmpty(note.getTitle())) {
            if (isEmpty(note.getContent())) {
                return false;
            }
            //note.setTitle(note.getContent());
        }

        return true;
    }

    public static boolean validateBeforeUpdate(Note note) {
        if (note == null || note.getId() <= 0 || note.getNotebook() <= 0) {
            return false;
        }

        if (isEmpty(note.getTitle())) {
            if (isEmpty(note.getContent())) {
                return false;
            }
            //note.setTitle(note.getContent());
        }

        return true;
    }

    public static boolean isValid(Note note) {
        return isValid(note, true);
    }

    public static boolean isValid(Note note, boolean checkId) {
        if (note == null || note.getNotebook() <= 0) {
            return false;
        }
        if (checkId) {
            if (note.getId() <= 0) {
                return false;
            }
        }
        if (isEmpty(note.getTitle())) {
            if (isEmpty(note.getContent())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValid(List<Note> notes) {
        if (notes == null) {
            return false;
        }
        for (Note note : notes) {
            if (!isValid(note)) {
                return false;
            }
        }
        return true;
    }

}

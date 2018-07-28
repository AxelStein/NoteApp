package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.Note;

import java.util.List;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class NoteValidator {

    public static boolean validateBeforeInsert(Note note) {
        return note != null && (notEmpty(note.getTitle()) || notEmpty(note.getContent()));
    }

    public static boolean validateBeforeUpdate(Note note) {
        return note != null && note.hasId() && (notEmpty(note.getTitle()) || notEmpty(note.getContent()));

    }

    public static boolean isValid(Note note) {
        return isValid(note, true);
    }

    public static boolean isValid(Note note, boolean checkId) {
        if (note == null) {
            return false;
        }
        if (checkId) {
            if (!note.hasId()) {
                return false;
            }
        }
        return notEmpty(note.getTitle()) || notEmpty(note.getContent());
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

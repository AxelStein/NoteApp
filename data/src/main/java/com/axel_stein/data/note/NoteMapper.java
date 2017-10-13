package com.axel_stein.data.note;

import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Note;

import java.util.ArrayList;
import java.util.List;

class NoteMapper {

    static Note map(@Nullable NoteEntity entity) {
        if (entity == null) {
            return null;
        }
        Note note = new Note();
        note.setId(entity.getId());
        note.setTitle(entity.getTitle());
        note.setContent(entity.getContent());
        note.setNotebook(entity.getNotebook());
        note.setRelevance(entity.getRelevance());
        note.setDate(entity.getDate());
        note.setUpdate(entity.getUpdate());
        note.setTrash(entity.isTrash());
        return note;
    }

    static NoteEntity map(@Nullable Note note) {
        if (note == null) {
            return null;
        }
        NoteEntity entity = new NoteEntity();
        entity.setId(note.getId());
        entity.setTitle(note.getTitle());
        entity.setContent(note.getContent());
        entity.setNotebook(note.getNotebook());
        entity.setRelevance(note.getRelevance());
        entity.setDate(note.getDate());
        entity.setUpdate(note.getUpdate());
        entity.setTrash(note.isTrash());
        return entity;
    }

    static List<Note> map(@Nullable List<NoteEntity> entities) {
        if (entities == null) {
            return null;
        }
        List<Note> notes = new ArrayList<>();
        for (NoteEntity e : entities) {
            notes.add(map(e));
        }
        return notes;
    }

    static List<Long> mapIds(@Nullable List<Note> notes) {
        if (notes == null) {
            return null;
        }
        List<Long> ids = new ArrayList<>();
        for (Note n : notes) {
            ids.add(n.getId());
        }
        return ids;
    }

}

package com.axel_stein.data.note;

import androidx.annotation.Nullable;

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
        note.setNotebookId(entity.getNotebookId());
        note.setViews(entity.getViews());
        note.setTrashed(entity.isTrashed());
        note.setTrashedDate(entity.getTrashedDate());
        note.setPinned(entity.isPinned());
        note.setStarred(entity.isStarred());
        note.setModifiedDate(entity.getModifiedDate());
        note.setCheckList(entity.isCheckList());
        note.setCheckListJson(entity.getCheckListJson());
        note.setHasReminder(entity.hasReminder());
        note.setReminderId(entity.getReminderId());
        note.setArchived(entity.isArchived());
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
        entity.setNotebookId(note.getNotebookId());
        entity.setViews(note.getViews());
        entity.setTrashed(note.isTrashed());
        entity.setTrashedDate(note.getTrashedDate());
        entity.setPinned(note.isPinned());
        entity.setStarred(note.isStarred());
        entity.setModifiedDate(note.getModifiedDate());
        entity.setCheckList(note.isCheckList());
        entity.setCheckListJson(note.getCheckListJson());
        entity.setHasReminder(note.hasReminder());
        entity.setReminderId(note.getReminderId());
        entity.setArchived(note.isArchived());
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

    static List<String> mapIds(@Nullable List<Note> notes) {
        if (notes == null) {
            return null;
        }
        List<String> ids = new ArrayList<>();
        for (Note n : notes) {
            ids.add(n.getId());
        }
        return ids;
    }

}

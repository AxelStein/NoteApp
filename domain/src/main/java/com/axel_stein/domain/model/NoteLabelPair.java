package com.axel_stein.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NoteLabelPair {

    @JsonProperty("note_id")
    private String noteId;

    @JsonProperty("label_id")
    private long labelId;

    private boolean trash;

    public NoteLabelPair() {

    }

    @JsonIgnore
    public NoteLabelPair(String noteId, long labelId, boolean trash) {
        this.noteId = noteId;
        this.labelId = labelId;
        this.trash = trash;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public long getLabelId() {
        return labelId;
    }

    public void setLabelId(long labelId) {
        this.labelId = labelId;
    }

    public boolean isTrash() {
        return trash;
    }

    public void setTrash(boolean trash) {
        this.trash = trash;
    }
}

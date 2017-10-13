package com.axel_stein.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NoteLabelPair {

    @JsonProperty("note_id")
    private long noteId;

    @JsonProperty("label_id")
    private long labelId;

    private boolean trash;

    public NoteLabelPair() {

    }

    @JsonIgnore
    public NoteLabelPair(long noteId, long labelId, boolean trash) {
        this.noteId = noteId;
        this.labelId = labelId;
        this.trash = trash;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
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

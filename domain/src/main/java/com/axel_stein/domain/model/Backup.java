package com.axel_stein.domain.model;

import com.axel_stein.domain.json_wrapper.NoteWrapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class Backup {

    private int version = 1;

    private List<NoteWrapper> notes;

    private List<Notebook> notebooks;

    private List<Label> labels;

    @JsonProperty("note_label_pairs")
    private List<NoteLabelPair> noteLabelPairs;

    public Backup() {
    }

    public int getVersion() {
        return version;
    }

    public List<NoteWrapper> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = new ArrayList<>();
        for (Note n : notes) {
            this.notes.add(new NoteWrapper(n));
        }
    }

    public List<Notebook> getNotebooks() {
        return notebooks;
    }

    public void setNotebooks(List<Notebook> notebooks) {
        this.notebooks = notebooks;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public List<NoteLabelPair> getNoteLabelPairs() {
        return noteLabelPairs;
    }

    public void setNoteLabelPairs(List<NoteLabelPair> noteLabelPairs) {
        this.noteLabelPairs = noteLabelPairs;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}

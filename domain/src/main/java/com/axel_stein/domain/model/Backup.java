package com.axel_stein.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class Backup {

    private List<Note> notes;

    private List<Notebook> notebooks;

    private List<Label> labels;

    @JsonProperty("note_label_pairs")
    private List<NoteLabelPair> noteLabelPairs;

    public Backup() {
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
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

package com.axel_stein.domain.model;

import com.axel_stein.domain.json_wrapper.LabelWrapper;
import com.axel_stein.domain.json_wrapper.NoteWrapper;
import com.axel_stein.domain.json_wrapper.NotebookWrapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class Backup {

    private int version = 2;

    private List<NoteWrapper> notes;

    private List<NotebookWrapper> notebooks;

    private List<LabelWrapper> labels;

    @JsonProperty("note_label_pairs")
    private List<NoteLabelPair> noteLabelPairs;

    @JsonProperty("settings")
    private String jsonSettings;

    public Backup() {
    }

    public int getVersion() {
        return version;
    }

    public List<NoteWrapper> getNotes() {
        return notes;
    }

    public void setSourceNotes(List<Note> notes) {
        this.notes = new ArrayList<>();
        for (Note note : notes) {
            this.notes.add(NoteWrapper.fromNote(note));
        }
    }

    public List<NotebookWrapper> getNotebooks() {
        return notebooks;
    }

    public void setSourceNotebooks(List<Notebook> notebooks) {
        this.notebooks = new ArrayList<>();
        for (Notebook notebook : notebooks) {
            this.notebooks.add(NotebookWrapper.fromNotebook(notebook));
        }
    }

    public List<LabelWrapper> getLabels() {
        return labels;
    }

    public void setSourceLabels(List<Label> labels) {
        this.labels = new ArrayList<>();
        for (Label label : labels) {
            this.labels.add(LabelWrapper.fromLabel(label));
        }
    }

    public List<NoteLabelPair> getNoteLabelPairs() {
        return noteLabelPairs;
    }

    public void setNoteLabelPairs(List<NoteLabelPair> noteLabelPairs) {
        this.noteLabelPairs = noteLabelPairs;
    }

    public void setJsonSettings(String json) {
        this.jsonSettings = json;
    }

    public String getJsonSettings() {
        return jsonSettings;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}

package com.axel_stein.domain.model;

import com.axel_stein.domain.json_wrapper.NoteWrapper;
import com.axel_stein.domain.json_wrapper.NotebookWrapper;
import com.axel_stein.domain.json_wrapper.ReminderWrapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class Backup {
    private final int version = 5;

    private List<NoteWrapper> notes;

    private List<NotebookWrapper> notebooks;

    @JsonProperty("settings")
    private String jsonSettings;

    private List<ReminderWrapper> reminders;

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

    public void setJsonSettings(String json) {
        this.jsonSettings = json;
    }

    public String getJsonSettings() {
        return jsonSettings;
    }

    public void setSourceReminders(List<Reminder> reminders) {
        this.reminders = new ArrayList<>();
        for (Reminder r : reminders) {
            this.reminders.add(ReminderWrapper.from(r));
        }
    }

    public List<ReminderWrapper> getReminders() {
        return reminders;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}

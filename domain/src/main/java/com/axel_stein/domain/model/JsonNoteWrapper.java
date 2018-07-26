package com.axel_stein.domain.model;

import com.axel_stein.domain.utils.TextUtil;

import org.joda.time.DateTime;

import java.util.UUID;

public class JsonNoteWrapper {

    private String id;

    private long notebook;

    private long relevance;

    private boolean trash;

    private String title;

    private String content;

    private String created;

    private String modified;

    private boolean pinned;

    public JsonNoteWrapper(Note note) {
        id = note.getId();
        notebook = note.getNotebook();
        relevance = note.getRelevance();
        trash = note.isTrash();
        title = note.getTitle();
        content = note.getContent();
        pinned = note.isPinned();

        DateTime c = note.getCreated();
        if (c != null) {
            created = c.toString();
        }

        DateTime m = note.getModified();
        if (m != null) {
            modified = m.toString();
        }
    }

    public Note toNote() {
        Note note = new Note();
        note.setId(TextUtil.isEmpty(id) ? UUID.randomUUID().toString() : id);
        note.setTitle(title);
        note.setContent(content);
        note.setPinned(pinned);
        note.setTrash(trash);
        note.setNotebook(notebook);
        note.setRelevance(relevance);

        note.setCreated(TextUtil.isEmpty(created) ? new DateTime() : DateTime.parse(created));
        note.setModified(TextUtil.isEmpty(modified) ? new DateTime() : DateTime.parse(modified));

        return note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getNotebook() {
        return notebook;
    }

    public void setNotebook(long notebook) {
        this.notebook = notebook;
    }

    public long getRelevance() {
        return relevance;
    }

    public void setRelevance(long relevance) {
        this.relevance = relevance;
    }

    public boolean isTrash() {
        return trash;
    }

    public void setTrash(boolean trash) {
        this.trash = trash;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

}

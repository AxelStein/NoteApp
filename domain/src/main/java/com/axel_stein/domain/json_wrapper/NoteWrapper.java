package com.axel_stein.domain.json_wrapper;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.utils.TextUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;

import java.util.UUID;

public class NoteWrapper {

    private String id;

    private String title;

    private String content;

    @JsonProperty("notebook_id")
    private String notebookId;

    private long views;

    private boolean pinned;

    private boolean starred;

    private boolean trashed;

    @JsonProperty("trashed_date")
    private String trashedDate;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("modified_date")
    private String modifiedDate;

    @JsonProperty("drive_id")
    private String driveId;

    public static NoteWrapper fromNote(Note note) {
        NoteWrapper wrapper = new NoteWrapper();
        wrapper.id = note.getId();
        wrapper.notebookId = note.getNotebookId();
        wrapper.views = note.getViews();
        wrapper.trashed = note.isTrashed();
        wrapper.title = note.getTitle();
        wrapper.content = note.getContent();
        wrapper.pinned = note.isPinned();
        wrapper.starred = note.isStarred();
        wrapper.driveId = note.getDriveId();

        DateTime t = note.getTrashedDate();
        if (t != null) {
            wrapper.trashedDate = t.toString();
        }

        DateTime c = note.getCreatedDate();
        if (c != null) {
            wrapper.createdDate = c.toString();
        }

        DateTime m = note.getModifiedDate();
        if (m != null) {
            wrapper.modifiedDate = m.toString();
        }
        return wrapper;
    }

    public Note toNote() {
        Note note = new Note();
        note.setId(TextUtil.isEmpty(id) ? UUID.randomUUID().toString() : id);
        note.setTitle(title);
        note.setContent(content);
        note.setNotebookId(notebookId);
        note.setViews(views);
        note.setPinned(pinned);
        note.setStarred(starred);
        note.setTrashed(trashed);
        note.setTrashedDate(TextUtil.isEmpty(trashedDate) ? new DateTime() : DateTime.parse(trashedDate));
        note.setCreatedDate(TextUtil.isEmpty(createdDate) ? new DateTime() : DateTime.parse(createdDate));
        note.setModifiedDate(TextUtil.isEmpty(modifiedDate) ? new DateTime() : DateTime.parse(modifiedDate));
        return note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(String notebookId) {
        this.notebookId = notebookId;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public String getTrashedDate() {
        return trashedDate;
    }

    public void setTrashedDate(String trashedDate) {
        this.trashedDate = trashedDate;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
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

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

}

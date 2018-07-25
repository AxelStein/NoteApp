package com.axel_stein.data.note;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey
    @NonNull
    private String id = "";

    @ColumnInfo
    private String title;

    @ColumnInfo
    private String content;

    @ColumnInfo
    private long notebook;

    @ColumnInfo
    private long relevance;

    @ColumnInfo
    private boolean trash;

    @ColumnInfo
    private boolean pinned;

    @ColumnInfo
    private DateTime created;

    @ColumnInfo
    private DateTime modified;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isPinned() {
        return pinned;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public DateTime getModified() {
        return modified;
    }

    public void setModified(DateTime modified) {
        this.modified = modified;
    }

}

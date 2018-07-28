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
    private String notebookId;

    @ColumnInfo
    private long views;

    @ColumnInfo
    private boolean pinned;

    @ColumnInfo
    private boolean trashed;

    @ColumnInfo
    private DateTime trashedDate;

    @ColumnInfo
    private boolean starred;

    @ColumnInfo
    private DateTime createdDate;

    @ColumnInfo
    private DateTime modifiedDate;

    @ColumnInfo
    private String driveId;

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

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isPinned() {
        return pinned;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public DateTime getTrashedDate() {
        return trashedDate;
    }

    public void setTrashedDate(DateTime trashedDate) {
        this.trashedDate = trashedDate;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

}

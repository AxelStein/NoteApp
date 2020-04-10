package com.axel_stein.data.note;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
    private DateTime modifiedDate;

    @ColumnInfo
    private boolean checkList;

    @ColumnInfo
    private String checkListJson;

    @ColumnInfo
    private boolean hasReminder;

    @ColumnInfo
    private String reminderId;

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

    public boolean isCheckList() {
        return checkList;
    }

    public void setCheckList(boolean checkList) {
        this.checkList = checkList;
    }

    public String getCheckListJson() {
        return checkListJson;
    }

    public void setCheckListJson(String checkListJson) {
        this.checkListJson = checkListJson;
    }

    public boolean hasReminder() {
        return hasReminder;
    }

    public void setHasReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
    }

    public String getReminderId() {
        return reminderId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

}

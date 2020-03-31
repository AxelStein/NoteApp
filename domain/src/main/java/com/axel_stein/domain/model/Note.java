package com.axel_stein.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.domain.utils.CompareBuilder;
import com.axel_stein.domain.utils.TextUtil;

import org.joda.time.DateTime;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class Note implements Cloneable {
    private String id;
    private String title;
    private String content;
    private String notebookId;
    private long views;
    private boolean pinned;
    private boolean starred;
    private boolean trashed;
    private DateTime trashedDate;
    private DateTime modifiedDate;
    private boolean checkList;
    private String checkListJson;

    public Note() {
    }

    public Note(String id) {
        this.id = id;
    }

    public boolean hasId() {
        return !TextUtil.isEmpty(id);
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

    public void setNotebook(@NonNull Notebook notebook) {
        requireNonNull(notebook);
        this.notebookId = notebook.getId();
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        if (views < 0) {
            throw new IllegalArgumentException("views should be >= 0");
        }
        this.views = views;
    }

    public void incrementViews() {
        this.views++;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    @NonNull
    public String getTitle() {
        if (title == null) {
            title = "";
        }
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getContent() {
        return content;
    }

    public void setContent(@Nullable String content) {
        this.content = content;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isPinned() {
        return pinned;
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

    public void setCheckList(boolean checkList) {
        this.checkList = checkList;
    }

    public boolean isCheckList() {
        return checkList;
    }

    public void setCheckListJson(String checkListJson) {
        this.checkListJson = checkListJson;
    }

    public String getCheckListJson() {
        return checkListJson;
    }

    public Note copy() {
        Note copy;
        try {
            copy = (Note) clone();
        } catch (Exception ex) {
            copy = new Note();
        }
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Note) {
            Note note = (Note) obj;

            CompareBuilder builder = new CompareBuilder();
            builder.append(id, note.id);
            builder.append(title, note.title);
            builder.append(content, note.content);
            builder.append(notebookId, note.notebookId);
            builder.append(modifiedDate, note.modifiedDate);
            builder.append(trashed, note.trashed);
            builder.append(pinned, note.pinned);
            builder.append(starred, note.starred);
            builder.append(checkList, note.checkList);
            builder.append(checkListJson, note.checkListJson);

            return builder.areEqual();
        }

        return false;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", notebookId='" + notebookId + '\'' +
                ", views=" + views +
                ", pinned=" + pinned +
                ", starred=" + starred +
                ", trashed=" + trashed +
                ", trashedDate=" + trashedDate +
                ", modifiedDate=" + modifiedDate +
                '}';
    }
}

package com.axel_stein.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.utils.CompareBuilder;
import com.axel_stein.domain.utils.TextUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.joda.time.DateTime;

public class Notebook {
    public static final int MAX_TITLE_LENGTH = 128;

    public static final String ID_ALL = "all";
    public static String TITLE_ALL;
    public static int ICON_ALL;

    public static final String ID_STARRED = "starred";
    public static String TITLE_STARRED;
    public static int ICON_STARRED;

    public static final String ID_INBOX = "inbox";
    public static String TITLE_INBOX;
    public static int ICON_INBOX;

    public static Notebook inbox() {
        Notebook n = new Notebook();
        n.setId(ID_INBOX);
        n.setTitle(TITLE_INBOX);
        n.iconRes = ICON_INBOX;
        return n;
    }

    public static Notebook all() {
        Notebook n = new Notebook();
        n.setId(ID_ALL);
        n.title = TITLE_ALL;
        n.iconRes = ICON_ALL;
        n.setEditable(false);
        return n;
    }

    public static Notebook starred() {
        Notebook n = new Notebook();
        n.setId(ID_STARRED);
        n.title = TITLE_STARRED;
        n.iconRes = ICON_STARRED;
        n.setEditable(false);
        return n;
    }

    private String id;

    private String title;

    private int order;

    private long views;

    private String color;

    private DateTime createdDate;

    private DateTime modifiedDate;

    private String driveId;

    @JsonIgnore
    private int iconRes;

    @JsonIgnore
    private boolean editable = true;

    @JsonIgnore
    private long noteCount;

    public boolean hasId() {
        return !TextUtil.isEmpty(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        if (title == null) {
            title = "";
        }
        return title;
    }

    public void setTitle(@Nullable String title) {
        if (title != null) {
            int length = title.length();
            if (length > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH);
            }
        }
        this.title = title;
    }

    public long getNoteCount() {
        return noteCount;
    }

    public void setNoteCount(long noteCount) {
        this.noteCount = noteCount;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long incrementViews() {
        this.views++;
        return this.views;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public int getIconRes() {
        return iconRes;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Notebook) {
            Notebook notebook = (Notebook) obj;

            CompareBuilder builder = new CompareBuilder();
            builder.append(id, notebook.id);
            return builder.areEqual();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Notebook{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", noteCount=" + noteCount +
                ", order=" + order +
                '}';
    }
}

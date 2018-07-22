package com.axel_stein.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.utils.CompareBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Label {
    public static final int MAX_TITLE_LENGTH = 128;

    private long id;

    private String title;

    @JsonIgnore
    private long noteCount;

    private int order;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Label) {
            Label label = (Label) obj;

            CompareBuilder builder = new CompareBuilder();
            builder.append(id, label.id);
            return builder.areEqual();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Label{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", noteCount=" + noteCount +
                ", order=" + order +
                '}';
    }
}

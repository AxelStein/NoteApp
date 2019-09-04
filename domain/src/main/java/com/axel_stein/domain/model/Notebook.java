package com.axel_stein.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.domain.utils.CompareBuilder;
import com.axel_stein.domain.utils.TextUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Notebook {
    public static final String ID_INBOX = "inbox";
    public static final String ID_ADD = "add";

    private String id;
    private String title;

    @JsonIgnore
    private int iconRes;

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
        this.title = title;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Notebook) {
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
                '}';
    }
}

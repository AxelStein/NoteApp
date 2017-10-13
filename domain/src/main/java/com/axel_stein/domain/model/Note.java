package com.axel_stein.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.utils.CompareBuilder;
import com.axel_stein.domain.utils.MapComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class Note implements Cloneable {

    public static final int MAX_TITLE_LENGTH = 128;

    private long id;

    private long notebook;

    private long relevance;

    private boolean trash;

    private String title;

    private String content;

    private long date;

    private long update;

    @Nullable
    private HashMap<Long, Boolean> labels;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getNotebook() {
        return notebook;
    }

    public void setNotebook(@NonNull Notebook notebook) {
        requireNonNull(notebook);
        this.notebook = notebook.getId();
    }

    public void setNotebook(long notebook) {
        this.notebook = notebook;
    }

    public long getRelevance() {
        return relevance;
    }

    public void setRelevance(long relevance) {
        if (relevance < 0) {
            throw new IllegalArgumentException("relevance should be >= 0");
        }
        this.relevance = relevance;
    }

    public void incrementRelevance() {
        relevance = relevance + 1;
    }

    public boolean isTrash() {
        return trash;
    }

    public void setTrash(boolean trash) {
        this.trash = trash;
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

    @Nullable
    public String getContent() {
        return content;
    }

    public void setContent(@Nullable String content) {
        this.content = content;
    }

    /**
     * @throws NullPointerException     if label is null
     * @throws IllegalArgumentException if label`s id is 0
     */
    public void addLabel(@NonNull Label label) {
        label = requireNonNull(label, "label is null");
        addLabel(label.getId());
    }

    /**
     * @throws IllegalArgumentException if label`s id is 0
     */
    public void addLabel(long labelId) {
        if (labelId <= 0) {
            throw new IllegalArgumentException("labelId is 0");
        }
        initLabels().put(labelId, true);
    }

    /**
     * @throws NullPointerException     if label is null
     * @throws IllegalArgumentException if label`s id is 0
     */
    public boolean containsLabel(@NonNull Label label) {
        label = requireNonNull(label, "label is null");
        return containsLabel(label.getId());
    }

    /**
     * @throws IllegalArgumentException if labelId is 0
     */
    public boolean containsLabel(long labelId) {
        if (labelId <= 0) {
            throw new IllegalArgumentException("labelId is 0");
        }
        return initLabels().containsKey(labelId);
    }

    /**
     * @throws NullPointerException     if label is null
     * @throws IllegalArgumentException if label`s id is 0
     */
    public void removeLabel(@NonNull Label label) {
        label = requireNonNull(label, "label is null");
        removeLabel(label.getId());
    }

    /**
     * @throws IllegalArgumentException if labelId is 0
     */
    public void removeLabel(long labelId) {
        if (labelId <= 0) {
            throw new IllegalArgumentException("labelId is 0");
        }
        initLabels().remove(labelId);
    }

    public void clearLabels() {
        initLabels().clear();
    }

    @NonNull
    private HashMap<Long, Boolean> initLabels() {
        if (labels == null) {
            labels = new HashMap<>();
        }
        return labels;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getUpdate() {
        return update;
    }

    public void setUpdate(long update) {
        this.update = update;
    }

    public boolean hasLabels() {
        return labels != null && labels.size() > 0;
    }

    @Nullable
    public List<Long> getLabels() {
        if (labels == null || labels.size() == 0) {
            return null;
        }
        List<Long> list = new ArrayList<>();
        for (long l : labels.keySet()) {
            list.add(l);
        }
        return list;
    }

    public void setLabels(@Nullable List<Long> labels) {
        clearLabels();
        if (labels != null) {
            for (long l : labels) {
                addLabel(l);
            }
        }
    }

    public Note copy() {
        Note copy;
        try {
            copy = (Note) clone();
            if (labels != null) {
                copy.labels = (HashMap<Long, Boolean>) labels.clone();
            }
        } catch (Exception ex) {
            copy = new Note();
        }
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Note) {
            Note note = (Note) obj;

            CompareBuilder builder = new CompareBuilder();
            builder.append(id, note.id);
            builder.append(title, note.title);
            builder.append(content, note.content);
            builder.append(notebook, note.notebook);
            builder.append(date, note.date);
            builder.append(update, note.update);
            builder.append(trash, note.trash);

            MapComparator<Long, Boolean> mapComparator = new MapComparator<>();
            builder.append(mapComparator.compare(labels, note.labels));

            return builder.areEqual();
        }

        return false;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", notebook=" + notebook +
                ", relevance=" + relevance +
                ", trash=" + trash +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", update=" + update +
                '}';
    }
}

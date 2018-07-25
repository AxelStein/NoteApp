package com.axel_stein.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.utils.CompareBuilder;
import com.axel_stein.domain.utils.MapComparator;
import com.axel_stein.domain.utils.TextUtil;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class Note implements Cloneable {

    public static final int MAX_TITLE_LENGTH = 128;

    private String id;

    private long notebook;

    private long relevance;

    private boolean trash;

    private String title;

    private String content;

    private DateTime created;

    private DateTime modified;

    private boolean pinned;

    @Nullable
    private HashMap<Long, Boolean> labels;

    public boolean hasId() {
        return !TextUtil.isEmpty(id);
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

    @Nullable
    public List<Long> getLabels() {
        if (labels == null || labels.size() == 0) {
            return null;
        }
        List<Long> list = new ArrayList<>();
        list.addAll(labels.keySet());
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

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isPinned() {
        return pinned;
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
            builder.append(created, note.created);
            builder.append(modified, note.modified);
            builder.append(trash, note.trash);
            builder.append(pinned, note.pinned);

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
                ", pinned=" + pinned +
                ", title='" + title + '\'' +
                ", created=" + created +
                ", modified=" + modified +
                '}';
    }

}

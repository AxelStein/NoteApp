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

    private String title;

    private String content;

    private String notebookId;

    private long views;

    private boolean pinned;

    private boolean starred;

    private boolean trashed;

    private DateTime trashedDate;

    private DateTime createdDate;

    private DateTime modifiedDate;

    private String driveId;

    @Nullable
    private HashMap<String, Boolean> labels;

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

    public long incrementViews() {
        this.views++;
        return this.views;
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
    public void addLabel(String labelId) {
        if (TextUtil.isEmpty(labelId)) {
            throw new IllegalArgumentException();
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
    public boolean containsLabel(String labelId) {
        if (TextUtil.isEmpty(labelId)) {
            throw new IllegalArgumentException();
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
    public void removeLabel(String labelId) {
        if (TextUtil.isEmpty(labelId)) {
            throw new IllegalArgumentException();
        }
        initLabels().remove(labelId);
    }

    public void clearLabels() {
        initLabels().clear();
    }

    @NonNull
    private HashMap<String, Boolean> initLabels() {
        if (labels == null) {
            labels = new HashMap<>();
        }
        return labels;
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

    @Nullable
    public List<String> getLabels() {
        if (labels == null || labels.size() == 0) {
            return null;
        }
        return new ArrayList<>(labels.keySet());
    }

    public void setLabels(@Nullable List<String> labels) {
        clearLabels();
        if (labels != null) {
            for (String l : labels) {
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

    public Note copy() {
        Note copy;
        try {
            copy = (Note) clone();
            if (labels != null) {
                copy.labels = (HashMap<String, Boolean>) labels.clone();
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
            builder.append(notebookId, note.notebookId);
            builder.append(createdDate, note.createdDate);
            builder.append(modifiedDate, note.modifiedDate);
            // todo builder.append(trashedDate, note.trashedDate);
            builder.append(trashed, note.trashed);
            builder.append(pinned, note.pinned);
            builder.append(starred, note.starred);
            // todo driveId
            // todo views

            MapComparator<String, Boolean> mapComparator = new MapComparator<>();
            builder.append(mapComparator.compare(labels, note.labels));

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
                ", createdDate=" + createdDate +
                ", modifiedDate=" + modifiedDate +
                ", driveId='" + driveId + '\'' +
                ", labels=" + labels +
                '}';
    }
}

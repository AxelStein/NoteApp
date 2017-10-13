package com.axel_stein.data.note_label_pair;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "note_label_pairs")
public class NoteLabelPairEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entity_id")
    private long id;

    @ColumnInfo(name = "note_id")
    private long noteId;

    @ColumnInfo(name = "label_id")
    private long labelId;

    @ColumnInfo(name = "entity_trash")
    private boolean trash;

    public NoteLabelPairEntity() {

    }

    @Ignore
    public NoteLabelPairEntity(long noteId, long labelId, boolean trash) {
        this.noteId = noteId;
        this.labelId = labelId;
        this.trash = trash;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLabelId() {
        return labelId;
    }

    public void setLabelId(long labelId) {
        this.labelId = labelId;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public boolean isTrash() {
        return trash;
    }

    public void setTrash(boolean trash) {
        this.trash = trash;
    }
}

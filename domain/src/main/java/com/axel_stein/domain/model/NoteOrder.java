package com.axel_stein.domain.model;

import androidx.annotation.Nullable;

public enum NoteOrder {
    TITLE,
    VIEWS,
    CREATED,
    MODIFIED,
    TRASHED;

    private boolean desc;

    @Nullable
    public static NoteOrder from(int value, boolean desc) {
        NoteOrder order = fromInt(value);
        if (order != null) {
            order.setDesc(desc);
        }
        return order;
    }

    @Nullable
    public static NoteOrder fromInt(int x) {
        switch (x) {
            case 0:
                return TITLE;

            case 1:
                return VIEWS;

            case 2:
                return CREATED;

            case 3:
                return MODIFIED;

            case 4:
                return TRASHED;
        }

        return null;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

}
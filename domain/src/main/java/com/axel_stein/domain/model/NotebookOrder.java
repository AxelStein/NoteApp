package com.axel_stein.domain.model;

import android.support.annotation.Nullable;

public enum NotebookOrder {
    TITLE,
    NOTE_COUNT,
    CUSTOM;

    private boolean desc;

    @Nullable
    public static NotebookOrder from(int value, boolean desc) {
        NotebookOrder order = fromInt(value);
        if (order != null) {
            order.setDesc(desc);
        }
        return order;
    }

    @Nullable
    private static NotebookOrder fromInt(int x) {
        switch (x) {
            case 0:
                return TITLE;

            case 1:
                return NOTE_COUNT;

            case 2:
                return CUSTOM;
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

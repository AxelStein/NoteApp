package com.axel_stein.domain.model;

import androidx.annotation.Nullable;

public enum LabelOrder {
    TITLE,
    NOTE_COUNT,
    CUSTOM;

    private boolean desc;

    @Nullable
    public static LabelOrder from(int value, boolean desc) {
        LabelOrder order = fromInt(value);
        if (order != null) {
            order.setDesc(desc);
        }
        return order;
    }


    @Nullable
    public static LabelOrder fromInt(int x) {
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

package com.axel_stein.domain.model;

import android.support.annotation.Nullable;

public enum  NotebookOrder {
    TITLE,
    NOTE_COUNT,
    CUSTOM;

    @Nullable
    public static NotebookOrder fromInt(int x) {
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
}

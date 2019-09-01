package com.axel_stein.domain.model;

import androidx.annotation.Nullable;

public enum NoteOrder {
    TITLE,
    VIEWS,
    MODIFIED,
    TRASHED;

    @Nullable
    public static NoteOrder from(int value) {
        return fromInt(value);
    }

    @Nullable
    private static NoteOrder fromInt(int x) {
        switch (x) {
            case 0:
                return TITLE;

            case 1:
                return VIEWS;

            case 2:
                return MODIFIED;

            case 3:
                return TRASHED;
        }

        return null;
    }

}
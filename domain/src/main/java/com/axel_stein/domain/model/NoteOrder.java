package com.axel_stein.domain.model;

import android.support.annotation.Nullable;

public enum NoteOrder {
    TITLE,
    RELEVANCE,
    CREATED_NEWEST,
    CREATED_OLDEST,
    MODIFIED_NEWEST,
    MODIFIED_OLDEST;

    @Nullable
    public static NoteOrder fromInt(int x) {
        switch (x) {
            case 0:
                return TITLE;

            case 1:
                return RELEVANCE;

            case 2:
                return CREATED_NEWEST;

            case 3:
                return CREATED_OLDEST;

            case 4:
                return MODIFIED_NEWEST;

            case 5:
                return MODIFIED_OLDEST;
        }

        return null;
    }

}
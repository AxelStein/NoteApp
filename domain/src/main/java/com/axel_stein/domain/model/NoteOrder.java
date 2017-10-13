package com.axel_stein.domain.model;

import android.support.annotation.Nullable;

public enum NoteOrder {
    TITLE,
    RELEVANCE,
    DATE_NEWEST,
    DATE_OLDEST,
    UPDATE_NEWEST,
    UPDATE_OLDEST;

    @Nullable
    public static NoteOrder fromInt(int x) {
        switch (x) {
            case 0:
                return TITLE;

            case 1:
                return RELEVANCE;

            case 2:
                return DATE_NEWEST;

            case 3:
                return DATE_OLDEST;

            case 4:
                return UPDATE_NEWEST;

            case 5:
                return UPDATE_OLDEST;
        }

        return null;
    }

}
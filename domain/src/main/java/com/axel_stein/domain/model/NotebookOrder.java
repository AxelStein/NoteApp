package com.axel_stein.domain.model;

import android.support.annotation.Nullable;

public enum  NotebookOrder {
    TITLE,
    CUSTOM;

    @Nullable
    public static NotebookOrder fromInt(int x) {
        switch (x) {
            case 0:
                return TITLE;

            case 1:
                return CUSTOM;
        }

        return null;
    }
}

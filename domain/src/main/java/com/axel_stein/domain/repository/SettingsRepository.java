package com.axel_stein.domain.repository;

import com.axel_stein.domain.model.NoteOrder;

public interface SettingsRepository {

    /**
     * @return notes order
     */
    NoteOrder getNotesOrder();

    void setNotesOrder(NoteOrder order);

    String defaultNotebookTitle();

}

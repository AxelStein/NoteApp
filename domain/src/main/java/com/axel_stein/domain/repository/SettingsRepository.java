package com.axel_stein.domain.repository;

import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.NotebookOrder;

public interface SettingsRepository {

    /**
     * @return notes order
     */
    NoteOrder getNotesOrder();

    void setNotesOrder(NoteOrder order);

    NotebookOrder getNotebookOrder();

    void setNotebookOrder(NotebookOrder order);

    String defaultNotebookTitle();

    boolean showNotesContent();

    void setShowNotesContent(boolean show);

}

package com.axel_stein.domain.repository;

import com.axel_stein.domain.model.LabelOrder;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.NotebookOrder;

public interface SettingsRepository {

    NoteOrder getNotesOrder();

    void setNotesOrder(NoteOrder order);

    NotebookOrder getNotebookOrder();

    void setNotebookOrder(NotebookOrder order);

    LabelOrder getLabelOrder();

    void setLabelOrder(LabelOrder order);

    boolean showNotesContent();

    void setShowNotesContent(boolean show);

    String exportSettings();

    void importSettings(String json);

}

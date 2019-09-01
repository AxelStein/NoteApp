package com.axel_stein.domain.repository;

import com.axel_stein.domain.model.NoteOrder;

public interface SettingsRepository {

    NoteOrder getNotesOrder();

    void setNotesOrder(NoteOrder order);

    boolean showNotesContent();

    void setShowNotesContent(boolean show);

    String exportSettings();

    void importSettings(String json);

}

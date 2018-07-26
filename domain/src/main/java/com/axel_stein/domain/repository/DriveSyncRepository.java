package com.axel_stein.domain.repository;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.model.Notebook;

import java.util.List;

public interface DriveSyncRepository {

    void notifyNoteChanged(Note note);

    void notifyNoteDeleted(Note note);

    void notifyNotebooksChanged(List<Notebook> data);

    void notifyLabelsChanged(List<Label> data);

    void notifyNoteLabelPairsChanged(List<NoteLabelPair> data);

    void notifySettingsChanged(String data);

}

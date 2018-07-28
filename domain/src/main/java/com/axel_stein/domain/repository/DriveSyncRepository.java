package com.axel_stein.domain.repository;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.model.Notebook;

import java.util.List;

public interface DriveSyncRepository {

    /* Notes */

    void noteCreated(Note note);

    void noteUpdated(Note note);

    void noteNotebookChanged(Note note);

    void noteViewed(Note note);

    void notePinned(Note note, boolean pinned);

    void notesPinned(List<Note> notes, boolean pinned);

    void noteStarred(Note note, boolean starred);

    void notesStarred(List<Note> notes, boolean starred);

    void noteTrashed(Note note, boolean trashed);

    void notesTrashed(List<Note> notes, boolean trashed);

    void noteDeleted(Note note);

    /* Notebooks */

    void notebookCreated(Notebook notebook);

    void notebookUpdated(Notebook notebook);

    void notebookRenamed(Notebook notebook);

    void notebookViewed(Notebook notebook);

    void notebookOrderChanged(Notebook notebook);

    void notebookColorChanged(Notebook notebook);

    void notebookDeleted(Notebook notebook);

    /* Labels */

    void labelCreated(Label label);

    void labelUpdated(Label label);

    void labelRenamed(Label label);

    void labelViewed(Label label);

    void labelOrderChanged(Label label);

    void labelDeleted(Label label);

    // fixme

    void notifyNoteLabelPairsChanged(List<NoteLabelPair> data);

    void notifySettingsChanged(String data);

}

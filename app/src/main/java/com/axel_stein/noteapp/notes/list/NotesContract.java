package com.axel_stein.noteapp.notes.list;

import android.support.annotation.MenuRes;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
// FIXME: 13.10.2017
public interface NotesContract {

    interface View {

        void setNotes(List<Note> notes);

        void showError();

        void startCheckMode();

        void onItemChecked(int pos, int checkCount);

        void stopCheckMode();

        void showNote(Note note);

        void showSelectNotebookView(List<Notebook> notebooks);

        void showCheckLabelsView(List<Label> labels);

        void showConfirmDeleteDialog();
    }

    interface Presenter {

        void onCreateView(View view);

        void onDestroyView();

        void onNoteClick(int pos, Note note);

        boolean onNoteLongClick(int pos, Note note);

        boolean isChecked(Note note);

        boolean hasChecked();

        @MenuRes
        int getCheckModeMenu();

        void onActionItemClicked(int itemId);

        void onNotebookSelected(Notebook notebook);

        void onLabelsChecked(List<Long> labels);

        void stopCheckMode();

        void confirmDelete();
    }

}


package com.axel_stein.noteapp.main.list;

import android.content.res.Resources;
import android.view.MenuItem;

import androidx.annotation.MenuRes;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface NotesContract {

    interface View {

        void setNotes(List<Note> notes);

        void showError();

        void startCheckMode();

        void onItemChecked(int pos, int checkCount);

        void stopCheckMode();

        void showNote(Note note, android.view.View view);

        void showSelectNotebookView(List<Notebook> notebooks);

        void showCheckLabelsView(List<Label> labels);

        void showConfirmDeleteDialog(List<Note> notes);

        void showSortDialog(int itemId);

        void scrollToTop();

        Resources getResources();
    }

    interface Presenter {

        void onCreateView(View view);

        void onDestroyView();

        void forceUpdate();

        void onNoteClick(int pos, Note note, android.view.View view);

        boolean onNoteLongClick(int pos, Note note);

        boolean hasSwipeLeftAction();

        boolean hasSwipeRightAction();

        void swipeLeft(Note note);

        void swipeRight(Note note);

        boolean isChecked(Note note);

        boolean hasChecked();

        @MenuRes
        int getCheckModeMenu();

        void onActionItemClicked(int itemId);

        void onNotebookSelected(Notebook notebook);

        void onLabelsChecked(List<String> labels);

        void stopCheckMode();

        void showSortMenu();

        void onSortMenuItemClick(MenuItem item);

    }

}


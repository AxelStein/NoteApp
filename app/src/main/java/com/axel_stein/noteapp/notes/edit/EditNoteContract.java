package com.axel_stein.noteapp.notes.edit;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;

import java.util.List;

public interface EditNoteContract {

    interface View {

        void setNote(Note note);

        void showDiscardChangesView();

        void showShareNoteView(Note note);

        void showMessage(int msg);

        void showSelectNotebookView(List<Notebook> notebooks, long selectedNotebook);

        void showCheckLabelsView(List<Label> labels, List<Long> checkedLabels);

        void showNoteInfoView(Note note);

        void showConfirmDeleteNoteView();

        void callFinish();

        void setEditable(boolean editable);

    }

    interface Presenter {

        void onCreateView(@NonNull View view);

        void onDestroyView();

        void setTitle(String title);

        void setContent(String content);

        void setNotebook(Notebook notebook);

        void setLabels(List<Long> labels);

        /**
         * @return true if should finish
         */
        boolean close();

        void confirmDiscardChanges();

        void save();

        void delete();

        void actionSelectNotebook();

        void actionCheckLabels();

        void actionMoveToTrash();

        void actionRestore();

        void actionDelete();

        void actionShare();

        void actionInfo();

        void addOnNoteChangedListener(OnNoteChangedListener l);

    }

    interface OnNoteChangedListener {
        void onNoteChanged(boolean changed);
    }

}

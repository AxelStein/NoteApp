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

        void showSelectNotebookView(List<Notebook> notebooks, String selectedNotebook);

        void showCheckLabelsView(List<Label> labels, List<String> checkedLabels);

        void showConfirmDeleteNoteView();

        void callFinish();

        void setEditable(boolean editable);

        boolean searchPanelShown();

        void hideSearchPanel();

        void setNotePinned(boolean pinned);

        void showNoteInfo(Note note);
    }

    interface Presenter {

        void onCreateView(@NonNull View view);

        void onDestroyView();

        void setTitle(String title);

        void setContent(String content);

        void setNotebook(Notebook notebook);

        void setLabels(List<String> labels);

        boolean onBackPressed();

        /**
         * @return true if should finish
         */
        boolean close();

        void confirmDiscardChanges();

        void save();

        void saveOrFinish();

        void delete();

        void actionPinNote();

        void actionSelectNotebook();

        void actionCheckLabels();

        void actionMoveToTrash();

        void actionRestore();

        void actionDelete();

        void actionShare();

        void actionDuplicate(String copySuffix);

        void addOnNoteChangedListener(OnNoteChangedListener l);

        void actionNoteInfo();
    }

    interface OnNoteChangedListener {
        void onNoteChanged(boolean changed);
    }

}

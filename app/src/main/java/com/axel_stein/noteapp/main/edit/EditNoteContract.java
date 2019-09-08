package com.axel_stein.noteapp.main.edit;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.main.edit.EditNotePresenter.CheckItem;

import java.util.List;

public interface EditNoteContract {

    interface View {

        void setNote(Note note);

        void setNoteCheckList(Note note, List<CheckItem> items);

        void showDiscardChangesView();

        void showShareNoteView(Note note);

        void showMessage(int msg);

        void showSelectNotebookView(List<Notebook> notebooks, String selectedNotebook);

        void showConfirmDeleteNoteView();

        void callFinish();

        void setEditable(boolean editable);

        void setNotePinned(boolean pinned);

        void setNoteStarred(boolean starred);

        void setNotebookTitle(String notebook);

        void clearFocus();

        void showCheckList(List<CheckItem> list);

        void hideCheckList();

        List<CheckItem> getCheckItems();

        void setTitle(String title);

        void setContent(String content);

    }

    interface Presenter {

        void onStop();

        void onCreateView(@NonNull View view);

        void onDestroyView();

        void setTitle(String title);

        void setContent(String content);

        void setNotebook(Notebook notebook);

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

        void actionMoveToTrash();

        void actionRestore();

        void actionDelete();

        void actionShare();

        void actionDuplicate(String copySuffix);

        void addOnNoteChangedListener(OnNoteChangedListener l);

        void actionStarNote();

        boolean isPinned();

        boolean isStarred();

        void actionCheckList();
    }

    interface OnNoteChangedListener {
        void onNoteChanged(boolean changed);
    }

}

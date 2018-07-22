package com.axel_stein.noteapp.notebook_manager;

import com.axel_stein.domain.model.Notebook;

import java.util.List;

public interface NotebookManagerContract {

    interface View {

        void setItems(List<Notebook> items);

        void confirmDeleteDialog(Notebook notebook);

        void startNoteListActivity(Notebook notebook);
    }

    interface Presenter {

        void onCreateView(View view);

        void onDestroyView();

        void forceUpdate();

        void onItemClick(int pos, Notebook notebook);

    }
}

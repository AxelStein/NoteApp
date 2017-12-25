package com.axel_stein.noteapp.notebook_manager;

import com.axel_stein.domain.model.Notebook;

import java.util.List;

public interface NotebookManagerContract {

    interface View {

        void setItems(List<Notebook> items);

        void startCheckMode();

        void onItemChecked(int pos, int checkCount);

        void stopCheckMode();

        void confirmDeleteDialog(List<Notebook> items);

    }

    interface Presenter {

        void onCreateView(View view);

        void onDestroyView();

        void forceUpdate();

        void onItemClick(int pos, Notebook notebook);

        boolean onItemLongClick(int pos, Notebook notebook);

        void startCheckMode();

        void stopCheckMode();

        boolean isChecked(Notebook notebook);

        boolean hasChecked();

        void onActionItemClicked(int itemId);

        boolean checkModeEnabled();
    }
}

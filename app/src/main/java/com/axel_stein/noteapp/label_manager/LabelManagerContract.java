package com.axel_stein.noteapp.label_manager;

import com.axel_stein.domain.model.Label;

import java.util.List;

public interface LabelManagerContract {

    interface View {

        void setItems(List<Label> items);

        void confirmDeleteDialog(Label label);

        void startNoteListActivity(Label label);
    }

    interface Presenter {

        void onCreateView(View view);

        void onDestroyView();

        void forceUpdate();

        void onItemClick(int pos, Label label);

    }

}

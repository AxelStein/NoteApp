package com.axel_stein.noteapp.base;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Notebook;

public interface Screen {

    void showTrashActivity();

    void addNotebookDialog();

    void renameNotebookDialog(@NonNull Notebook notebook);

    void deleteNotebookDialog(@NonNull Notebook notebook);

    void addLabelDialog();

    void renameLabelDialog(@NonNull Label label);

    void deleteLabelDialog(@NonNull Label label);

    void showSettingsActivity();

    void showSearchActivity();

}

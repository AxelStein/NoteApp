package com.axel_stein.noteapp.backup;

import com.axel_stein.domain.model.BackupFile;

import java.util.List;

public interface BackupContract {

    interface View {

        void setItems(List<BackupFile> items);

        void showImportDialog();

        void dismissImportDialog();

    }

    interface Presenter {

        void onCreateView(View view);

        void onDestroyView();

        void onFileClick(BackupFile file);

        void addFile();

        void importFile(BackupFile file);

        void renameFile(BackupFile file, String name);

        void deleteFile(BackupFile file);
    }

}

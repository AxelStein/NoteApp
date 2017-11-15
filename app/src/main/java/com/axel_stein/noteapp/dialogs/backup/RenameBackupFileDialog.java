package com.axel_stein.noteapp.dialogs.backup;

import com.axel_stein.domain.model.BackupFile;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.EditTextDialog;
import com.axel_stein.noteapp.utils.FileUtil;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class RenameBackupFileDialog extends EditTextDialog {

    public static RenameBackupFileDialog create(BackupFile file) {
        checkNotNull(file);

        RenameBackupFileDialog dialog = new RenameBackupFileDialog();
        dialog.mFile = file;
        dialog.setHint(R.string.hint_label);
        dialog.setTitle(R.string.title_rename_label);
        dialog.setText(file.getName());
        dialog.setPositiveButtonText(R.string.action_rename);
        dialog.setNegativeButtonText(R.string.action_cancel);
        return dialog;
    }

    private BackupFile mFile;

    @Override
    protected void onTextCommit(String text) {
        FileUtil.rename(mFile.getFile(), text);
    }

}

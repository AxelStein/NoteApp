package com.axel_stein.noteapp.dialogs.drive;

import androidx.appcompat.app.AppCompatActivity;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;

public class ConfirmImportDialog extends ConfirmDialog {
    public static final String TAG = "ConfirmImportDialog";

    public static void launch(AppCompatActivity activity) {
        createDialog().show(activity.getSupportFragmentManager(), TAG);
    }

    private static ConfirmImportDialog createDialog() {
        ConfirmImportDialog dialog = new ConfirmImportDialog();
        dialog.setTitle(R.string.title_import_drive);
        dialog.setMessage(R.string.msg_import_drive);
        dialog.setPositiveButtonText(R.string.action_import);
        dialog.setNegativeButtonText(R.string.action_cancel);
        return dialog;
    }

}

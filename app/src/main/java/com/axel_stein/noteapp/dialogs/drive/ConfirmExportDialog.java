package com.axel_stein.noteapp.dialogs.drive;

import androidx.appcompat.app.AppCompatActivity;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;

public class ConfirmExportDialog extends ConfirmDialog {
    public static final String TAG = "ConfirmExportDialog";

    public static void launch(AppCompatActivity activity) {
        createDialog().show(activity.getSupportFragmentManager(), TAG);
    }

    private static ConfirmExportDialog createDialog() {
        ConfirmExportDialog dialog = new ConfirmExportDialog();
        dialog.setTitle(R.string.title_export_drive);
        dialog.setMessage(R.string.msg_export_drive);
        dialog.setPositiveButtonText(R.string.action_export);
        dialog.setNegativeButtonText(R.string.action_cancel);
        return dialog;
    }

}

package com.axel_stein.noteapp.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.R;

public class NoteInfoDialog extends AppCompatDialogFragment {

    private Note mNote;

    public void setNote(Note note) {
        this.mNote = note;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // , R.style.DialogStyle
        builder.setTitle(R.string.title_note_info);
        builder.setMessage(getMessage());
        builder.setNegativeButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private String getMessage() {
        if (mNote == null) {
            return null;
        }

        String created = mNote.getCreated().toString();
        String modified = mNote.getModified().toString();
        // fixme
        //String created = DateFormatter.formatDateTime(getContext(), mNote.getCreated());
        //String modified = DateFormatter.formatDateTime(getContext(), mNote.getModified());
        int charCount = mNote.getContent() == null ? 0 : mNote.getContent().length();

        return getString(R.string.msg_note_info, created, modified, charCount);
    }

    public void show(Fragment fragment, String tag) {
        setTargetFragment(fragment, 0);
        show(fragment.getFragmentManager(), tag);
    }

}

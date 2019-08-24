package com.axel_stein.noteapp.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.DateFormatter;

import org.joda.time.DateTime;

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

        String created = null;
        DateTime createdDate = mNote.getCreatedDate();
        if (createdDate != null) {
            created = DateFormatter.formatDateTime(getContext(), createdDate.getMillis());
        }

        String modified = null;
        DateTime modifiedDate = mNote.getModifiedDate();
        if (modifiedDate != null) {
            modified = DateFormatter.formatDateTime(getContext(), modifiedDate.getMillis());
        }
        int charCount = mNote.getContent() == null ? 0 : mNote.getContent().length();

        long views = mNote.getViews();

        DateTime trashedDate = mNote.getTrashedDate();
        if (trashedDate != null) {
            String trashed = DateFormatter.formatDateTime(getContext(), trashedDate.getMillis());
            return getString(R.string.msg_note_info_trashed, created, modified, trashed, views, charCount);
        }
        return getString(R.string.msg_note_info, created, modified, views, charCount);
    }

    public void show(Fragment fragment, String tag) {
        setTargetFragment(fragment, 0);
        show(fragment.getFragmentManager(), tag);
    }

}

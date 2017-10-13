package com.axel_stein.noteapp.dialogs.note;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.DateFormatter;
import com.axel_stein.noteapp.utils.ResourceUtil;

public class NoteInfoDialog extends AppCompatDialogFragment {

    private String mTitle;
    private int mTitleRes;
    private String mMessage;
    private int mMessageRes;
    private String mPositiveButtonText;
    private int mPositiveButtonTextRes;
    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;

    public static void launch(Context context, Fragment fragment, Note note) {
        NoteInfoDialog dialog = new NoteInfoDialog();
        dialog.setTitle(R.string.action_info);
        dialog.setMessage(context.getString(R.string.note_info,
                DateFormatter.formatDateTime(context, note.getDate()),
                DateFormatter.formatDateTime(context, note.getUpdate())));
        /*
        dialog.setMessage(String.format(Locale.ROOT,
                "Created: %s\nUpdated: %s\nRelevance: %d",
                DateFormatter.formatDateTime(context, note.getDate()),
                DateFormatter.formatDateTime(context, note.getUpdate()),
                note.getRelevance()));
        */
        dialog.show(fragment.getFragmentManager(), null);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTitle(@StringRes int titleRes) {
        mTitleRes = titleRes;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public void setMessage(@StringRes int messageRes) {
        mMessageRes = messageRes;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(@StringRes int positiveButtonTextRes) {
        mPositiveButtonTextRes = positiveButtonTextRes;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(@StringRes int negativeButtonTextRes) {
        mNegativeButtonTextRes = negativeButtonTextRes;
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
        builder.setTitle(getText(mTitle, mTitleRes));
        builder.setMessage(getText(mMessage, mMessageRes));
        builder.setPositiveButton(getText(mPositiveButtonText, mPositiveButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        builder.setNegativeButton(getText(mNegativeButtonText, mNegativeButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        return builder.create();
    }

    private String getText(String s, int res) {
        return ResourceUtil.getString(getContext(), s, res);
    }

    public void show(FragmentManager manager) {
        show(manager, getClass().getSimpleName());
    }

}

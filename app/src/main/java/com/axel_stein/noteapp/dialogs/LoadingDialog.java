package com.axel_stein.noteapp.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialogFragment;

import com.axel_stein.noteapp.utils.ResourceUtil;

public class LoadingDialog extends AppCompatDialogFragment {
    private static final String TAG = LoadingDialog.class.getSimpleName();
    private String mTitle;
    private int mTitleRes;
    private String mMessage;
    private int mMessageRes;

    public static LoadingDialog from(int titleRes, int messageRes) {
        LoadingDialog dialog = new LoadingDialog();
        dialog.setTitle(titleRes);
        dialog.setMessage(messageRes);
        return dialog;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTitle(int titleRes) {
        mTitleRes = titleRes;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public void setMessage(int messageRes) {
        mMessageRes = messageRes;
    }

    private String getText(String s, int res) {
        return ResourceUtil.getString(getContext(), s, res);
    }

    public void show(FragmentManager manager) {
        show(manager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setTitle(getText(mTitle, mTitleRes));
        dialog.setMessage(getText(mMessage, mMessageRes));
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
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
}


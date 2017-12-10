package com.axel_stein.noteapp.dialogs.notebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ResourceUtil;

import java.util.List;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class SelectNotebookDialog extends AppCompatDialogFragment {

    private String mTitle;
    private int mTitleRes;
    private String mPositiveButtonText;
    private int mPositiveButtonTextRes;
    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;
    private List<Notebook> mNotebooks;
    private String[] mNotebookTitles;
    private int mSelectedPosition = -1;
    private OnNotebookSelectedListener mListener;

    public static void launch(Fragment fragment, List<Notebook> notebooks, long selectedNotebook) {
        SelectNotebookDialog dialog = new SelectNotebookDialog();
        dialog.setTitle(R.string.title_select_notebook);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.setNotebooks(notebooks, selectedNotebook);
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), null);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTitle(int titleRes) {
        mTitleRes = titleRes;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(int positiveButtonTextRes) {
        mPositiveButtonTextRes = positiveButtonTextRes;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextRes) {
        mNegativeButtonTextRes = negativeButtonTextRes;
    }

    public void setNotebooks(List<Notebook> notebooks, long selectedNotebookId) {
        mNotebooks = checkNotNull(notebooks);

        mNotebookTitles = new String[notebooks.size()];
        for (int i = 0; i < notebooks.size(); i++) {
            Notebook notebook = notebooks.get(i);
            mNotebookTitles[i] = notebook.getTitle();
            if (selectedNotebookId == notebook.getId()) {
                mSelectedPosition = i;
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        App.getAppComponent().inject(this);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        Fragment fragment = getTargetFragment();

        if (activity instanceof OnNotebookSelectedListener) {
            mListener = (OnNotebookSelectedListener) activity;
        } else if (fragment != null && fragment instanceof OnNotebookSelectedListener) {
            mListener = (OnNotebookSelectedListener) fragment;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResourceText(mTitle, mTitleRes));
        builder.setNegativeButton(getResourceText(mNegativeButtonText, mNegativeButtonTextRes), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (mNotebookTitles != null) {
            builder.setSingleChoiceItems(mNotebookTitles, mSelectedPosition, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i != mSelectedPosition && mListener != null && mNotebooks != null) {
                        mSelectedPosition = i;
                        mListener.onNotebookSelected(mNotebooks.get(mSelectedPosition));
                    }
                    dismiss();
                }
            });
        }

        return builder.create();
    }

    private String getResourceText(String s, int res) {
        return ResourceUtil.getString(getContext(), s, res);
    }

    public interface OnNotebookSelectedListener {
        void onNotebookSelected(Notebook notebook);
    }

}

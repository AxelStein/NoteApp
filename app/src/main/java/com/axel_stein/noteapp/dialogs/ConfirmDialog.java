package com.axel_stein.noteapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.axel_stein.noteapp.utils.ResourceUtil;

public class ConfirmDialog extends AppCompatDialogFragment implements DialogInterface.OnClickListener {

    private String mTitle;
    private int mTitleRes;
    private String mMessage;
    private int mMessageRes;
    private String mPositiveButtonText;
    private int mPositiveButtonTextRes;
    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;
    private OnConfirmListener mOnConfirmListener;

    public static ConfirmDialog from(@StringRes int title, @StringRes int msg, @StringRes int positive, @StringRes int negative) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setPositiveButtonText(positive);
        dialog.setNegativeButtonText(negative);
        return dialog;
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
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = getTargetFragment();
        if (fragment != null) {
            setListeners(fragment);
        } else {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                setListeners(activity);
            }
        }
    }

    private void setListeners(Object o) {
        if (o instanceof OnConfirmListener) {
            mOnConfirmListener = (OnConfirmListener) o;
        }
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
        mOnConfirmListener = null;
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // , R.style.DialogStyle
        builder.setTitle(getText(mTitle, mTitleRes));
        builder.setMessage(getText(mMessage, mMessageRes));
        builder.setPositiveButton(getText(mPositiveButtonText, mPositiveButtonTextRes), this);
        builder.setNegativeButton(getText(mNegativeButtonText, mNegativeButtonTextRes), this);
        return builder.create();
    }

    private String getText(String s, int res) {
        return ResourceUtil.getString(getContext(), s, res);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            onConfirm();
            if (mOnConfirmListener != null) {
                mOnConfirmListener.onConfirm(getTag());
            }
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            if (mOnConfirmListener != null) {
                mOnConfirmListener.onCancel(getTag());
            }
        }
    }

    protected void onConfirm() {
    }

    public void show(Fragment fragment, String tag) {
        setTargetFragment(fragment, 0);
        show(fragment.getFragmentManager(), tag);
    }

    public interface OnConfirmListener {
        void onConfirm(String tag);

        void onCancel(String tag);
    }

}


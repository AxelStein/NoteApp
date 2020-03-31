package com.axel_stein.noteapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.axel_stein.noteapp.utils.ResourceUtil;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class ConfirmDialog extends AppCompatDialogFragment implements DialogInterface.OnClickListener {
    private static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
    private static final String BUNDLE_TITLE = "BUNDLE_TITLE";
    private static final String BUNDLE_POSITIVE = "BUNDLE_POSITIVE";
    private static final String BUNDLE_NEGATIVE = "BUNDLE_NEGATIVE";

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
    public void onAttach(@NonNull Context context) {
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_MESSAGE, getResourceText(mMessage, mMessageRes));
        outState.putString(BUNDLE_TITLE, getResourceText(mTitle, mTitleRes));
        outState.putString(BUNDLE_POSITIVE, getResourceText(mPositiveButtonText, mPositiveButtonTextRes));
        outState.putString(BUNDLE_NEGATIVE, getResourceText(mNegativeButtonText, mNegativeButtonTextRes));
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
        checkNotNull(getContext());

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(BUNDLE_TITLE);
            mPositiveButtonText = savedInstanceState.getString(BUNDLE_POSITIVE);
            mNegativeButtonText = savedInstanceState.getString(BUNDLE_NEGATIVE);
            mMessage = savedInstanceState.getString(BUNDLE_MESSAGE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // , R.style.DialogStyle
        builder.setTitle(getResourceText(mTitle, mTitleRes));
        builder.setMessage(getResourceText(mMessage, mMessageRes));
        builder.setPositiveButton(getResourceText(mPositiveButtonText, mPositiveButtonTextRes), this);
        builder.setNegativeButton(getResourceText(mNegativeButtonText, mNegativeButtonTextRes), this);
        return builder.create();
    }

    private String getResourceText(String s, int res) {
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
        assert fragment.getFragmentManager() != null;
        show(fragment.getFragmentManager(), tag);
    }

    public interface OnConfirmListener {
        void onConfirm(String tag);
        void onCancel(String tag);
    }

}


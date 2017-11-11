package com.axel_stein.noteapp.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ResourceUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;

public class PasswordDialog extends AppCompatDialogFragment {

    private String mTitle;
    private int mTitleRes;

    private String mPositiveButtonText;
    private int mPositiveButtonTextRes;

    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;

    private String mText = "";
    private boolean mShowMessage;

    private TextInputLayout mTextInputLayout;
    private TextInputEditText mEditText;
    private Button mPositiveButton;
    private boolean mPositiveButtonEnabled = false;

    private OnPasswordCommitListener mOnPasswordCommitListener;

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

    public void setText(String text) {
        mText = text;
        if (mText == null) {
            mText = "";
        }
    }

    public void setShowMessage(boolean showMessage) {
        mShowMessage = showMessage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        if (o instanceof OnPasswordCommitListener) {
            mOnPasswordCommitListener = (OnPasswordCommitListener) o;
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }

        mTextInputLayout = null;
        mEditText = null;
        mPositiveButton = null;
        mOnPasswordCommitListener = null;

        super.onDestroyView();
    }

    protected void setError(String error) {
        mTextInputLayout.setError(error);
    }

    protected void setError(@StringRes int errorRes) {
        setError(getString(errorRes));
    }

    protected void onTextCommit(String text) {
        if (mOnPasswordCommitListener != null) {
            mOnPasswordCommitListener.onPasswordCommit(text);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // R.style.DialogStyle
        builder.setTitle(getResourceText(mTitle, mTitleRes));
        builder.setPositiveButton(getResourceText(mPositiveButtonText, mPositiveButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onTextCommit(mEditText.getText().toString());
            }
        });
        builder.setNegativeButton(getResourceText(mNegativeButtonText, mNegativeButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(inflateView());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mPositiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                enablePositiveButton(mPositiveButtonEnabled);
            }
        });
        return dialog;
    }

    @SuppressLint("InflateParams")
    private View inflateView() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_password, null);

        ViewUtil.show(mShowMessage, root.findViewById(R.id.text_msg));

        mTextInputLayout = root.findViewById(R.id.text_input_password);

        mEditText = root.findViewById(R.id.edit_password);
        mEditText.setText(mText);
        mEditText.setSelection(mEditText.length());
        mEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                boolean empty = text.length() == 0;
                boolean equals = text.contentEquals(mText);

                enablePositiveButton(!empty && !equals);
            }
        });
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN && clickOnPositiveButton();
            }
        });
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_DONE && clickOnPositiveButton();
            }
        });
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Window window = getDialog().getWindow();
                    if (window != null) {
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            }
        });

        return root;
    }

    private String getResourceText(String s, int res) {
        return ResourceUtil.getString(getContext(), s, res);
    }

    private void enablePositiveButton(boolean enable) {
        mPositiveButtonEnabled = enable;
        if (mPositiveButton != null) {
            mPositiveButton.setEnabled(enable);
        }
    }

    private boolean clickOnPositiveButton() {
        if (mPositiveButton != null && mPositiveButton.isEnabled()) {
            mPositiveButton.performClick();
            return true;
        }
        return false;
    }

    public interface OnPasswordCommitListener {
        void onPasswordCommit(String password);
    }

}

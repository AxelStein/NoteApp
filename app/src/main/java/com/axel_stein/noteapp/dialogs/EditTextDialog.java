package com.axel_stein.noteapp.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ResourceUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;

// FIXME: 30.07.2017
public abstract class EditTextDialog extends AppCompatDialogFragment {
    private String mHint;
    private int mHintRes;

    private String mTitle;
    private int mTitleRes;

    private String mPositiveButtonText;
    private int mPositiveButtonTextRes;

    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;

    private String mText = "";
    private HashMap<String, Boolean> mSuggestions;

    private TextInputLayout mTextInputLayout;
    private TextInputEditText mEditText;
    private Button mPositiveButton;
    private boolean mPositiveButtonEnabled = false;

    public void setHint(String hint) {
        mHint = hint;
    }

    public void setHint(int hintRes) {
        mHintRes = hintRes;
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

    public void setText(String text) {
        mText = text;
        if (mText == null) {
            mText = "";
        }
    }

    public void setSuggestions(HashMap<String, Boolean> suggestions) {
        mSuggestions = suggestions;
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

        mTextInputLayout = null;
        mEditText = null;
        mPositiveButton = null;

        super.onDestroyView();
    }

    protected void setError(String error) {
        mTextInputLayout.setError(error);
    }

    protected void setError(@StringRes int errorRes) {
        setError(getString(errorRes));
    }

    protected abstract void onTextCommit(String text);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // R.style.DialogStyle
        builder.setTitle(getResourceText(mTitle, mTitleRes));
        builder.setPositiveButton(getResourceText(mPositiveButtonText, mPositiveButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onTextCommit(mEditText.getText().toString().trim());
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null);

        mTextInputLayout = view.findViewById(R.id.text_input);
        mTextInputLayout.setHint(getResourceText(mHint, mHintRes));

        mEditText = view.findViewById(R.id.edit_text);
        mEditText.setText(mText);
        mEditText.setSelection(mEditText.length());
        mEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                boolean empty = text.length() == 0;
                boolean lessThanMaxLength = text.length() <= mTextInputLayout.getCounterMaxLength();
                boolean equals = text.contentEquals(mText);

                enablePositiveButton(!empty && !equals && lessThanMaxLength);

                if (empty) {
                    mTextInputLayout.setError(getString(R.string.error_title_required));
                } else {
                    if (mSuggestions != null) {
                        if (!equals && mSuggestions.containsKey(text)) {
                            mTextInputLayout.setError(getString(R.string.error_title_exists));
                            enablePositiveButton(false);
                            return;
                        }
                    }
                    mTextInputLayout.setErrorEnabled(false);
                }
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

        return view;
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

}


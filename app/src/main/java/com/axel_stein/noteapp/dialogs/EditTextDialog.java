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

import java.util.ArrayList;
import java.util.HashMap;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public abstract class EditTextDialog extends AppCompatDialogFragment {
    private static final String BUNDLE_HINT = "BUNDLE_HINT";
    private static final String BUNDLE_TITLE = "BUNDLE_TITLE";
    private static final String BUNDLE_POSITIVE = "BUNDLE_POSITIVE";
    private static final String BUNDLE_NEGATIVE = "BUNDLE_NEGATIVE";
    private static final String BUNDLE_TEXT = "BUNDLE_TEXT";
    private static final String BUNDLE_SUGGESTIONS = "BUNDLE_SUGGESTIONS";

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

    protected void setSuggestions(HashMap<String, Boolean> suggestions) {
        mSuggestions = suggestions;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_HINT, getResourceText(mHint, mHintRes));
        outState.putString(BUNDLE_TITLE, getResourceText(mTitle, mTitleRes));
        outState.putString(BUNDLE_POSITIVE, getResourceText(mPositiveButtonText, mPositiveButtonTextRes));
        outState.putString(BUNDLE_NEGATIVE, getResourceText(mNegativeButtonText, mNegativeButtonTextRes));
        outState.putString(BUNDLE_TEXT, mText);

        if (mSuggestions != null) {
            outState.putStringArrayList(BUNDLE_SUGGESTIONS, new ArrayList<>(mSuggestions.keySet()));
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

        super.onDestroyView();
    }

    private void setError(String error) {
        mTextInputLayout.setError(error);
    }

    protected void setError(@StringRes int errorRes) {
        setError(getString(errorRes));
    }

    protected abstract void onTextCommit(String text);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        checkNotNull(getContext());

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(BUNDLE_TITLE);
            mPositiveButtonText = savedInstanceState.getString(BUNDLE_POSITIVE);
            mNegativeButtonText = savedInstanceState.getString(BUNDLE_NEGATIVE);
            mHint = savedInstanceState.getString(BUNDLE_HINT);
            mText = savedInstanceState.getString(BUNDLE_TEXT);

            mSuggestions = new HashMap<>();
            ArrayList<String> list = savedInstanceState.getStringArrayList(BUNDLE_SUGGESTIONS);
            if (list != null) {
                for (String s : list) {
                    mSuggestions.put(s, true);
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // R.style.DialogStyle
        builder.setTitle(getResourceText(mTitle, mTitleRes));
        builder.setPositiveButton(getResourceText(mPositiveButtonText, mPositiveButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editable e = mEditText.getText();
                if (e != null) {
                    onTextCommit(e.toString().trim());
                }
            }
        });
        builder.setNegativeButton(getResourceText(mNegativeButtonText, mNegativeButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(inflateView(savedInstanceState != null));

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
    private View inflateView(boolean restore) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null);

        mTextInputLayout = view.findViewById(R.id.text_input);
        mTextInputLayout.setHint(getResourceText(mHint, mHintRes));

        mEditText = view.findViewById(R.id.edit_text);
        if (!restore) {
            mEditText.setText(mText);
            mEditText.setSelection(mEditText.length());
        }
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
                    Dialog d = getDialog();
                    if (d != null) {
                        Window window = d.getWindow();
                        if (window != null) {
                            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
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


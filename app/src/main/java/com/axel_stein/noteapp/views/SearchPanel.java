package com.axel_stein.noteapp.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;

import java.util.Locale;

public class SearchPanel extends LinearLayout implements View.OnClickListener {

    private EditText mEditSearch;

    private TextView mTextResultCount;

    private ImageButton mButtonPrev;

    private ImageButton mButtonNext;

    private Callback mCallback;

    private int mCursor;

    private int mResultCount;

    private Runnable mTask;

    private String mCurrentQuery;

    public SearchPanel(Context context) {
        super(context);
    }

    public SearchPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final Handler handler = new Handler(Looper.getMainLooper());
        mTask = new Runnable() {
            @Override
            public void run() {
                String query = mEditSearch.getText().toString();
                if (mCallback != null) {
                    mCallback.onQueryTextChange(query);
                }
            }
        };

        mEditSearch = findViewById(R.id.edit_search);
        mEditSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.equals(s, mCurrentQuery)) {
                    mCurrentQuery = s.toString();

                    handler.removeCallbacks(mTask);
                    handler.postDelayed(mTask, 500);
                }
            }
        });
        mEditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        mTextResultCount = findViewById(R.id.text_result_count);

        mButtonPrev = findViewById(R.id.button_prev);
        mButtonPrev.setOnClickListener(this);

        mButtonNext = findViewById(R.id.button_next);
        mButtonNext.setOnClickListener(this);

        ImageButton buttonClose = findViewById(R.id.button_close);
        buttonClose.setOnClickListener(this);

        setQueryResultCount(mResultCount);
        setCursor(mCursor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_prev:
                setCursor(mCursor - 1);
                break;

            case R.id.button_next:
                setCursor(mCursor + 1);
                break;

            case R.id.button_close:
                ViewUtil.hide(this);

                mEditSearch.setText(null);
                hideKeyboard();

                if (mCallback != null) {
                    mCallback.onClose();
                }
                break;
        }
    }

    public String getQuery() {
        return mCurrentQuery == null ? "" : mCurrentQuery;
    }

    public void show() {
        ViewUtil.show(this);
        showKeyboard();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setQueryResultCount(int resultCount) {
        if (resultCount >= 0) {
            mResultCount = resultCount;
            setCursor(resultCount == 0 ? 0 : 1);
            ViewUtil.enable(resultCount > 1, mButtonPrev, mButtonNext);
        }
    }

    private void setCursor(int cursor) {
        if (cursor <= 0) {
            cursor = mResultCount;
        } else if (cursor > mResultCount) {
            cursor = mResultCount > 0 ? 1 : 0;
        }

        mCursor = cursor;
        mTextResultCount.setText(String.format(Locale.ROOT, "%d/%d", mCursor, mResultCount));

        if (mCursor > 0 && mCallback != null) {
            mCallback.onCursorChange(mCursor);
        }
    }

    private void showKeyboard() {
        mEditSearch.requestFocus();
        KeyboardUtil.show(mEditSearch);
    }

    private void hideKeyboard() {
        mEditSearch.clearFocus();
        requestFocus();
        KeyboardUtil.hide(mEditSearch);
    }

    static class SavedState extends BaseSavedState {
        int cursor;
        int resultCount;
        boolean shown;
        String currentQuery;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            cursor = in.readInt();
            resultCount = in.readInt();
            shown = in.readByte() != 0;
            currentQuery = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(cursor);
            out.writeInt(resultCount);
            out.writeByte((byte) (shown ? 1 : 0));
            out.writeString(currentQuery);
        }

        static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.cursor = mCursor;
        ss.resultCount = mResultCount;
        ss.shown = ViewUtil.isShown(this);
        ss.currentQuery = mCurrentQuery;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        mCurrentQuery = ss.currentQuery;

        super.onRestoreInstanceState(ss.getSuperState());

        setQueryResultCount(ss.resultCount);
        setCursor(ss.cursor);
        ViewUtil.show(ss.shown, this);
    }

    public interface Callback {

        void onQueryTextChange(String q);

        void onCursorChange(int cursor);

        void onClose();

    }

}

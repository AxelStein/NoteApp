package com.axel_stein.noteapp.notes.list;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.notes.list.presenters.SearchNotesPresenter;
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.edit_search)
    EditText mEditSearch;

    @BindView(R.id.button_clear)
    ImageButton mButtonClear;

    private NotesFragment mFragment;
    private Runnable mSearchTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        EventBusHelper.subscribe(this);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("fragment");
        if (fragment == null) {
            mFragment = new NotesFragment();
            mFragment.setEmptyMsg(getString(R.string.empty_search));
            setFragment(mFragment, "fragment");
        } else {
            mFragment = (NotesFragment) fragment;
        }

        mButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditSearch.setText(null);
                mEditSearch.requestFocus();
                KeyboardUtil.show(mEditSearch);
            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        mSearchTask = new Runnable() {
            @Override
            public void run() {
                String query = mEditSearch.getText().toString();
                if (TextUtils.isEmpty(query)) {
                    mFragment.setNotes(null);
                } else {
                    mFragment.setPresenter(new SearchNotesPresenter(query));
                }
            }
        };

        mEditSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(mSearchTask);
                handler.postDelayed(mSearchTask, 400);

                boolean empty = TextUtils.isEmpty(s);
                ViewUtil.show(!empty, mButtonClear);
            }
        });
        mEditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    v.clearFocus();
                    return true;
                }
                return false;
            }
        });
        mEditSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    KeyboardUtil.hide(SearchActivity.this);
                } else {
                    KeyboardUtil.show(v);
                }
            }
        });
        mEditSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditSearch.requestFocus();
            }
        });

        if (mEditSearch.length() == 0) {
            mEditSearch.requestFocus();
        }

        ViewUtil.show(mEditSearch.length() > 0, mButtonClear);
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        mEditSearch.clearFocus();
    }

    @Override
    protected void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        hideKeyboard();
        super.onStop();
    }

    private void hideKeyboard() {
        KeyboardUtil.hide(this);
    }

    @Subscribe
    public void showMessageEvent(EventBusHelper.Message e) {
        if (e.isRes()) {
            showMessage(e.getMsgRes());
        } else {
            showMessage(e.getMsg());
        }
    }

    private void showMessage(final String msg) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Snackbar.make(findViewById(R.id.coordinator_search), msg, Snackbar.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                }
            }
        }, 100);
    }

    private void showMessage(int msgRes) {
        showMessage(getString(msgRes));
    }

}

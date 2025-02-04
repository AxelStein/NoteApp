package com.axel_stein.noteapp.main.list;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.main.list.presenters.SearchNotesPresenter;
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;

public class SearchActivity extends BaseActivity {
    private static final String BUNDLE_SEARCH_HAS_FOCUS = "BUNDLE_SEARCH_HAS_FOCUS";
    private static final String BUNDLE_CURRENT_QUERY = "BUNDLE_CURRENT_QUERY";
    private static final int SEARCH_INPUT_DELAY = 600;

    @Nullable
    private NotesFragment mFragment;

    @Nullable
    private Runnable mSearchTask;

    private EditText mEditSearch;
    private ImageButton mButtonClear;
    private SimpleTextWatcher mTextWatcher;
    private String mCurrentQuery;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        EventBusHelper.subscribe(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("fragment_search");
        if (fragment == null) {
            mFragment = new NotesFragment();
            mFragment.setEmptyMsg(getString(R.string.empty_notes));
            mFragment.setPaddingTop(8);
            mFragment.setPaddingBottom(8);
            setFragment(mFragment, "fragment_search");
        } else {
            mFragment = (NotesFragment) fragment;
        }

        mEditSearch = findViewById(R.id.edit_search);
        mButtonClear = findViewById(R.id.button_clear);
        mButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditSearch.setText(null);
                if (!mEditSearch.hasFocus()) {
                    mEditSearch.requestFocus();
                }
                KeyboardUtil.show(mEditSearch);
            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        mSearchTask = new Runnable() {
            @Override
            public void run() {
                String query = mEditSearch.getText().toString();
                if (TextUtils.isEmpty(query)) {
                    mFragment.setPresenter(null);
                } else {
                    mFragment.setPresenter(new SearchNotesPresenter(query));
                }
            }
        };

        mTextWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.equals(s, mCurrentQuery)) {
                    mCurrentQuery = s.toString();

                    handler.removeCallbacks(mSearchTask);
                    handler.postDelayed(mSearchTask, SEARCH_INPUT_DELAY);

                    boolean empty = TextUtils.isEmpty(s);
                    ViewUtil.setVisible(!empty, mButtonClear);
                }
            }
        };
        mEditSearch.addTextChangedListener(mTextWatcher);
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
        if (savedInstanceState != null) {
            mCurrentQuery = savedInstanceState.getString(BUNDLE_CURRENT_QUERY);
            mEditSearch.setText(mCurrentQuery);
            if (mFragment != null && mFragment.getPresenter() == null) {
                handler.removeCallbacks(mSearchTask);
                handler.postDelayed(mSearchTask, SEARCH_INPUT_DELAY);
            }
        }

        if (mEditSearch.length() == 0) {
            mEditSearch.requestFocus();
        }

        ViewUtil.setVisible(mEditSearch.length() > 0, mButtonClear);

        setupAds();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_SEARCH_HAS_FOCUS, mEditSearch.hasFocus());
        outState.putString(BUNDLE_CURRENT_QUERY, mEditSearch.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean hasFocus = savedInstanceState.getBoolean(BUNDLE_SEARCH_HAS_FOCUS, false);
        if (!hasFocus) {
            mEditSearch.post(new Runnable() {
                @Override
                public void run() {
                    mEditSearch.clearFocus();
                }
            });
        }
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        mEditSearch.clearFocus();
    }

    @Override
    protected void onStop() {
        hideKeyboard();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mEditSearch.removeTextChangedListener(mTextWatcher);
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    private void hideKeyboard() {
        KeyboardUtil.hide(this);
    }

    @Subscribe
    public void showMessageEvent(final EventBusHelper.Message e) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    String msg = e.getMsg();
                    if (e.hasMsgRes()) {
                        msg = getString(e.getMsgRes());
                    }

                    String actionName = null;
                    if (e.hasActionNameRes()) {
                        actionName = getString(e.getActionName());
                    }

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_search), msg, Snackbar.LENGTH_SHORT);
                    if (e.hasAction()) {
                        snackbar.setAction(actionName, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                e.getAction().run();
                            }
                        });
                    }
                    snackbar.show();
                } catch (Exception ignored) {
                }
            }
        }, 100);
    }

}

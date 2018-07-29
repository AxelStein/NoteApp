package com.axel_stein.noteapp.notes.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.SwipeBaseActivity;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.IconTextView;
import com.axel_stein.noteapp.views.SearchPanel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class EditNoteActivity extends SwipeBaseActivity {

    public static final String EXTRA_NOTE_ID = "com.axel_stein.noteapp.EXTRA_NOTE_ID";

    public static final String EXTRA_NOTEBOOK_ID = "com.axel_stein.noteapp.EXTRA_NOTEBOOK_ID";

    public static final String EXTRA_LABEL_ID = "com.axel_stein.noteapp.EXTRA_LABEL_ID";

    private static final String BUNDLE_FULLSCREEN = "BUNDLE_FULLSCREEN";

    private static final String BUNDLE_KEEP_SCREEN_ON = "BUNDLE_KEEP_SCREEN_ON";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.search_panel)
    SearchPanel mSearchPanel;

    @BindView(R.id.notebook)
    IconTextView mNotebookView;

    @Inject
    GetNoteInteractor mGetNoteInteractor;

    @Inject
    AppSettingsRepository mAppSettings;

    @Nullable
    private EditNotePresenter mPresenter;

    private EditNoteFragment mEditNoteFragment;

    private boolean mKeepScreenOn;

    public static void launch(Context context) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        context.startActivity(intent);
    }

    public static void launch(Context context, Notebook notebook) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTEBOOK_ID, notebook.getId());
        context.startActivity(intent);
    }

    public static void launch(Context context, Label label) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_LABEL_ID, label.getId());
        context.startActivity(intent);
    }

    public static void launch(Context context, @NonNull Note note) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, note.getId());
        context.startActivity(intent);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        setHandleHomeButton(true);

        App.getAppComponent().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("fragment");
        if (fragment != null) {
            mEditNoteFragment = (EditNoteFragment) fragment;
            mEditNoteFragment.setSearchPanel(mToolbar, mSearchPanel);
            mEditNoteFragment.setNotebookView(mNotebookView);
            mPresenter = (EditNotePresenter) mEditNoteFragment.getPresenter();
            setPresenterListener();
        } else {
            Intent intent = getIntent();
            String id = intent.getStringExtra(EXTRA_NOTE_ID);
            final String notebookId = intent.getStringExtra(EXTRA_NOTEBOOK_ID);
            final String labelId = intent.getStringExtra(EXTRA_LABEL_ID);

            mGetNoteInteractor.execute(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Note>() {
                        @Override
                        public void accept(Note note) {
                            mPresenter = new EditNotePresenter(note);
                            setPresenterListener();

                            if (!note.hasId()) {
                                if (notEmpty(notebookId)) {
                                    note.setNotebookId(notebookId);
                                }
                                if (notEmpty(labelId)) {
                                    note.addLabel(labelId);
                                }
                            }

                            mEditNoteFragment = new EditNoteFragment();
                            mEditNoteFragment.setSearchPanel(mToolbar, mSearchPanel);
                            mEditNoteFragment.setNotebookView(mNotebookView);
                            mEditNoteFragment.setPresenter(mPresenter);

                            setFragment(mEditNoteFragment, "fragment");

                            handleIntent();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            throwable.printStackTrace();
                            EventBusHelper.showMessage(R.string.error);
                        }
                    });
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            return;
        }
        String type = intent.getType();
        if (type == null) {
            return;
        }
        if (mPresenter == null) {
            return;
        }

        if (Intent.ACTION_SEND.equals(action)) {
            if ("text/plain".equals(type)) {
                String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                mPresenter.setTitle(title);

                String content = intent.getStringExtra(Intent.EXTRA_TEXT);
                mPresenter.setContent(content);
            }
        }
    }

    private void setPresenterListener() {
        if (mPresenter != null) {
            mPresenter.addOnNoteChangedListener(new EditNoteContract.OnNoteChangedListener() {
                @Override
                public void onNoteChanged(boolean changed) {
                    if (mToolbar != null) {
                        int backIconRes = mNightMode ? R.drawable.ic_arrow_back_white_24dp : R.drawable.ic_arrow_back_black_24dp;
                        mToolbar.setNavigationIcon(changed ? R.drawable.ic_done_accent_24dp : backIconRes);
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean(BUNDLE_FULLSCREEN, !ViewUtil.isShown(mToolbar));
            outState.putBoolean(BUNDLE_KEEP_SCREEN_ON, mKeepScreenOn);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            boolean fullscreen = savedInstanceState.getBoolean(BUNDLE_FULLSCREEN);

            ViewUtil.show(!fullscreen, mToolbar);

            mKeepScreenOn = savedInstanceState.getBoolean(BUNDLE_KEEP_SCREEN_ON);
            updateWindowFlagsKeepScreenOn(mKeepScreenOn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_note, menu);

        mToolbar.post(new Runnable() {
            @Override
            public void run() {
                if (menu != null) {
                    updateKeepScreenOnItem(menu.findItem(R.id.menu_keep_screen_on));
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mPresenter != null) {
                    mPresenter.saveOrFinish();
                }
                return true;

            case R.id.menu_fullscreen:
                ViewUtil.hide(mToolbar);

                if (mAppSettings.showExitFullscreenMessage()) {
                    Toast.makeText(this, R.string.msg_exit_fullscreen, Toast.LENGTH_SHORT).show();
                }

                return true;

            case R.id.menu_keep_screen_on:
                mKeepScreenOn = !mKeepScreenOn;

                updateKeepScreenOnItem(item);

                updateWindowFlagsKeepScreenOn(mKeepScreenOn);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateKeepScreenOnItem(MenuItem item) {
        int colorAttr = mKeepScreenOn ? R.attr.keepScreenOnColor : R.attr.menuItemTintColor;
        MenuUtil.tintAttr(this, item, colorAttr);
    }

    /*
    @Override
    protected void onStart() {
        super.onStart();
        updateWindowFlagsKeepScreenOn(mKeepScreenOn);
    }

    @Override
    protected void onStop() {
        updateWindowFlagsKeepScreenOn(false);
        super.onStop();
    }
    */

    @Override
    protected void onDestroy() {
        updateWindowFlagsKeepScreenOn(false);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mPresenter == null || mPresenter.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void updateWindowFlagsKeepScreenOn(boolean keepScreenOn) {
        Window window = getWindow();
        if (window != null) {
            if (keepScreenOn) {
                window.addFlags(FLAG_KEEP_SCREEN_ON);
            } else {
                window.clearFlags(FLAG_KEEP_SCREEN_ON);
            }
        }
    }

}

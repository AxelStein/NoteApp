package com.axel_stein.noteapp.notes.edit;

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
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class EditNoteActivity extends BaseActivity {

    public static final String EXTRA_NOTE_ID = "com.axel_stein.noteapp.EXTRA_NOTE_ID";

    public static final String EXTRA_NOTEBOOK_ID = "com.axel_stein.noteapp.EXTRA_NOTEBOOK_ID";

    public static final String EXTRA_LABEL_ID = "com.axel_stein.noteapp.EXTRA_LABEL_ID";

    private static final String BUNDLE_FULLSCREEN = "BUNDLE_FULLSCREEN";

    private static final String BUNDLE_KEEP_SCREEN_ON = "BUNDLE_KEEP_SCREEN_ON";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Inject
    GetNoteInteractor mGetNoteInteractor;

    @Inject
    AppSettingsRepository mAppSettings;

    @Nullable
    private EditNotePresenter mPresenter;

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
            EditNoteFragment edit = (EditNoteFragment) fragment;
            mPresenter = (EditNotePresenter) edit.getPresenter();
            setPresenterListener();
        } else {
            Intent intent = getIntent();
            long id = intent.getLongExtra(EXTRA_NOTE_ID, 0);
            final long notebook = intent.getLongExtra(EXTRA_NOTEBOOK_ID, 0);
            final long label = intent.getLongExtra(EXTRA_LABEL_ID, 0);

            mGetNoteInteractor.execute(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Note>() {
                        @Override
                        public void accept(Note note) throws Exception {
                            mPresenter = new EditNotePresenter(note);
                            setPresenterListener();

                            if (note.getId() <= 0) {
                                if (notebook > 0) {
                                    note.setNotebook(notebook);
                                }
                                if (label > 0) {
                                    note.addLabel(label);
                                }
                            }

                            EditNoteFragment edit = new EditNoteFragment();
                            edit.setPresenter(mPresenter);

                            setFragment(edit, "fragment");
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            EventBusHelper.showMessage(R.string.error);
                        }
                    });
        }
    }

    private void setPresenterListener() {
        if (mPresenter != null) {
            mPresenter.addOnNoteChangedListener(new EditNoteContract.OnNoteChangedListener() {
                @Override
                public void onNoteChanged(boolean changed) {
                    if (mToolbar != null) {
                        int clearIconRes = mNightMode ? R.drawable.ic_clear_white_24dp : R.drawable.ic_clear_black_24dp;
                        int backIconRes = mNightMode ? R.drawable.ic_arrow_back_white_24dp : R.drawable.ic_arrow_back_black_24dp;
                        mToolbar.setNavigationIcon(changed ? clearIconRes : backIconRes);
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_FULLSCREEN, !ViewUtil.isShown(mToolbar));
        outState.putBoolean(BUNDLE_KEEP_SCREEN_ON, mKeepScreenOn);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        boolean fullscreen = savedInstanceState.getBoolean(BUNDLE_FULLSCREEN);

        ViewUtil.show(!fullscreen, mToolbar);
        ViewUtil.show(!fullscreen, findViewById(R.id.bottom));

        mKeepScreenOn = savedInstanceState.getBoolean(BUNDLE_KEEP_SCREEN_ON);
        updateWindowFlagsKeepScreenOn(mKeepScreenOn);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_note, menu);

        mToolbar.post(new Runnable() {
            @Override
            public void run() {
                int colorAttr = mKeepScreenOn ? R.attr.colorAccent : R.attr.menuItemTintColor;
                if (menu != null) {
                    MenuUtil.tintAttr(EditNoteActivity.this, menu.findItem(R.id.menu_keep_screen_on), colorAttr);
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
                onBackPressed();
                return true;

            case R.id.menu_fullscreen:
                ViewUtil.hide(mToolbar);
                ViewUtil.hide(findViewById(R.id.bottom));

                if (mAppSettings.showExitFullscreenMessage()) {
                    Toast.makeText(this, R.string.msg_exit_fullscreen, Toast.LENGTH_SHORT).show();
                }

                return true;

            case R.id.menu_keep_screen_on:
                mKeepScreenOn = !mKeepScreenOn;

                int colorAttr = mKeepScreenOn ? R.attr.colorAccent : R.attr.menuItemTintColor;
                MenuUtil.tintAttr(this, item, colorAttr);

                updateWindowFlagsKeepScreenOn(mKeepScreenOn);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    todo
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
        if (!ViewUtil.isShown(mToolbar)) {
            ViewUtil.show(mToolbar);
            ViewUtil.show(findViewById(R.id.bottom));
        } else if (mPresenter == null || mPresenter.close()) {
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

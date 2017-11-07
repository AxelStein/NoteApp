package com.axel_stein.noteapp.notes.edit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.utils.ViewUtil;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class EditNoteActivity extends BaseActivity {

    public static final String EXTRA_NOTE_ID = "com.axel_stein.noteapp.EXTRA_NOTE_ID";

    public static final String EXTRA_NOTEBOOK_ID = "com.axel_stein.noteapp.EXTRA_NOTEBOOK_ID";

    public static final String EXTRA_LABEL_ID = "com.axel_stein.noteapp.EXTRA_LABEL_ID";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Inject
    GetNoteInteractor mGetNoteInteractor;

    @Nullable
    private EditNotePresenter mPresenter;

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
        outState.putBoolean("fullscreen", !ViewUtil.isShown(mToolbar));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        boolean fullscreen = savedInstanceState.getBoolean("fullscreen");

        ViewUtil.show(!fullscreen, mToolbar);
        ViewUtil.show(!fullscreen, findViewById(R.id.bottom));
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

                SharedPreferences pref = getSharedPreferences("edit_note_activity", MODE_PRIVATE);
                boolean showMsg = pref.getBoolean("show_exit_fullscreen_msg", true);
                if (showMsg) {
                    Toast.makeText(this, R.string.msg_exit_fullscreen, Toast.LENGTH_SHORT).show();
                    pref.edit().putBoolean("show_exit_fullscreen_msg", false).apply();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!ViewUtil.isShown(mToolbar)) {
            ViewUtil.show(mToolbar);
            ViewUtil.show(findViewById(R.id.bottom));
        } else if (mPresenter != null && mPresenter.close()) {
            super.onBackPressed();
        }
    }

}

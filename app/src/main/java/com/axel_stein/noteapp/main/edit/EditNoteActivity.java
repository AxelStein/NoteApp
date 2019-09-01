package com.axel_stein.noteapp.main.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.views.IconTextView;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class EditNoteActivity extends BaseActivity {
    private static final String EXTRA_NOTE_ID = "com.axel_stein.noteapp.EXTRA_NOTE_ID";
    private static final String EXTRA_NOTEBOOK_ID = "com.axel_stein.noteapp.EXTRA_NOTEBOOK_ID";

    private Toolbar mToolbar;
    private IconTextView mNotebookView;

    @Inject
    GetNoteInteractor mGetNoteInteractor;

    @Inject
    AppSettingsRepository mAppSettings;

    @Nullable
    private EditNotePresenter mPresenter;
    private EditNoteFragment mEditNoteFragment;

    public static void launch(Context context, @Nullable String notebookId) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTEBOOK_ID, notebookId);
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

        App.getAppComponent().inject(this);

        mToolbar = findViewById(R.id.toolbar);
        mNotebookView = findViewById(R.id.notebook);

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
            mEditNoteFragment.setNotebookView(mNotebookView);
            mPresenter = (EditNotePresenter) mEditNoteFragment.getPresenter();
            setPresenterListener();
        } else {
            Intent intent = getIntent();
            String id = intent.getStringExtra(EXTRA_NOTE_ID);
            final String notebookId = intent.getStringExtra(EXTRA_NOTEBOOK_ID);

            mGetNoteInteractor.execute(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Note>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Note note) {
                            mPresenter = new EditNotePresenter(note);
                            setPresenterListener();

                            if (!note.hasId()) {
                                if (notEmpty(notebookId)) {
                                    note.setNotebookId(notebookId);
                                }
                            }

                            mEditNoteFragment = new EditNoteFragment();
                            mEditNoteFragment.setNotebookView(mNotebookView);
                            mEditNoteFragment.setPresenter(mPresenter);

                            setFragment(mEditNoteFragment, "fragment");

                            handleIntent();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mPresenter != null) {
                mPresenter.saveOrFinish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mPresenter == null || mPresenter.onBackPressed()) {
            super.onBackPressed();
        }
    }

}

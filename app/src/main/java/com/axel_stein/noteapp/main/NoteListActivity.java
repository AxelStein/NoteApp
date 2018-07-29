package com.axel_stein.noteapp.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.SwipeBaseActivity;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.notes.list.NotesFragment;
import com.axel_stein.noteapp.notes.list.presenters.LabelNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotebookNotesPresenter;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.IconTextView;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;

public class NoteListActivity extends SwipeBaseActivity implements SortPanelListener {
    private static final String EXTRA_NOTEBOOK_ID = "com.axel_stein.noteapp.main.EXTRA_NOTEBOOK_ID";
    private static final String EXTRA_NOTEBOOK_TITLE = "com.axel_stein.noteapp.main.EXTRA_NOTEBOOK_TITLE";
    private static final String EXTRA_NOTEBOOK_EDITABLE = "com.axel_stein.noteapp.main.EXTRA_NOTEBOOK_EDITABLE";

    private static final String EXTRA_LABEL_ID = "com.axel_stein.noteapp.main.EXTRA_LABEL_ID";
    private static final String EXTRA_LABEL_TITLE = "com.axel_stein.noteapp.main.EXTRA_LABEL_TITLE";

    public static void launch(Context context, Notebook notebook) {
        Intent intent = new Intent(context, NoteListActivity.class);
        intent.putExtra(EXTRA_NOTEBOOK_ID, notebook.getId());
        intent.putExtra(EXTRA_NOTEBOOK_TITLE, notebook.getTitle());
        intent.putExtra(EXTRA_NOTEBOOK_EDITABLE, notebook.isEditable());
        context.startActivity(intent);
    }

    public static void launch(Context context, Label label) {
        Intent intent = new Intent(context, NoteListActivity.class);
        intent.putExtra(EXTRA_LABEL_ID, label.getId());
        intent.putExtra(EXTRA_LABEL_TITLE, label.getTitle());
        context.startActivity(intent);
    }

    @BindView(R.id.app_bar)
    AppBarLayout mAppBar;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab_add)
    FloatingActionButton mFAB;

    @BindView(R.id.sort_panel)
    View mSortPanel;

    @BindView(R.id.text_item_counter)
    TextView mTextCounter;

    @BindView(R.id.text_sort)
    IconTextView mSortTitle;

    @Nullable
    private Notebook mNotebook;

    @Nullable
    private Label mLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_NOTEBOOK_ID)) {
            mNotebook = new Notebook();
            mNotebook.setId(intent.getStringExtra(EXTRA_NOTEBOOK_ID));
            mNotebook.setTitle(intent.getStringExtra(EXTRA_NOTEBOOK_TITLE));
            mNotebook.setEditable(intent.getBooleanExtra(EXTRA_NOTEBOOK_EDITABLE, true));
        } else if (intent.hasExtra(EXTRA_LABEL_ID)) {
            mLabel = new Label();
            mLabel.setId(intent.getStringExtra(EXTRA_LABEL_ID));
            mLabel.setTitle(intent.getStringExtra(EXTRA_LABEL_TITLE));
        }

        App.getAppComponent().inject(this);
        ButterKnife.bind(this);
        EventBusHelper.subscribe(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);

            String title = "";
            if (mNotebook != null) {
                title = mNotebook.getTitle();
            } else if (mLabel != null) {
                title = mLabel.getTitle();
            }
            setTitle(title);
        }

        setToolbarScrollFlags(true);

        FragmentManager fm = getSupportFragmentManager();
        NotesFragment fragment = (NotesFragment) fm.findFragmentByTag("fragment");

        if (fragment == null) {
            fragment = new NotesFragment();
            fragment.setPaddingTop(8);
            fragment.setPaddingBottom(88);

            int msg = 0;
            if (mNotebook != null) {
                msg = R.string.empty_notebook;
            } else if (mLabel != null) {
                msg = R.string.empty_label;
            }

            try {
                fragment.setEmptyMsg(getString(msg));
            } catch (Exception ignored) {
            }

            if (mNotebook != null) {
                fragment.setPresenter(new NotebookNotesPresenter(mNotebook));
            } else if (mLabel != null) {
                fragment.setPresenter(new LabelNotesPresenter(mLabel));
            }

            setFragment(fragment, "fragment");
        }

        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNotebook != null) {
                    EditNoteActivity.launch(NoteListActivity.this, mNotebook);
                } else if (mLabel != null) {
                    EditNoteActivity.launch(NoteListActivity.this, mLabel);
                } else {
                    EditNoteActivity.launch(NoteListActivity.this);
                }
            }
        });
    }

    private void setTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Subscribe
    public void showMessage(final EventBusHelper.Message e) {
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

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_note_list), msg, Snackbar.LENGTH_SHORT);
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

    @Subscribe
    public void onRecreate(EventBusHelper.Recreate e) {
        recreate();
    }

    @Subscribe
    public void renameNotebook(EventBusHelper.RenameNotebook e) {
        if (mNotebook != null) {
            Notebook n = e.getNotebook();
            if (mNotebook.equals(n)) {
                String title = n.getTitle();
                mNotebook.setTitle(title);
                setTitle(title);
            }
        }
    }

    @Subscribe
    public void deleteNotebook(EventBusHelper.DeleteNotebook e) {
        finish();
    }

    @Subscribe
    public void renameLabel(EventBusHelper.RenameLabel e) {
        if (mLabel != null) {
            Label l = e.getLabel();
            if (mLabel.equals(l)) {
                String title = l.getTitle();
                mLabel.setTitle(title);
                setTitle(title);
            }
        }
    }

    @Subscribe
    public void deleteLabel(EventBusHelper.DeleteLabel e) {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_note_list, menu);
        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);

        MenuUtil.showGroup(menu,R.id.menu_group_notebook, mNotebook != null && mNotebook.isEditable());
        MenuUtil.showGroup(menu,R.id.menu_group_label, mLabel != null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                showSearchActivity();
                break;

            case R.id.menu_rename_notebook:
                if (mNotebook != null) {
                    renameNotebookDialog(mNotebook);
                }
                break;

            case R.id.menu_delete_notebook:
                if (mNotebook != null) {
                    deleteNotebookDialog(mNotebook);
                }
                break;

            case R.id.menu_rename_label:
                if (mLabel != null) {
                    renameLabelDialog(mLabel);
                }
                break;

            case R.id.menu_delete_label:
                if (mLabel != null) {
                    deleteLabelDialog(mLabel);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View getSortPanel() {
        return mSortPanel;
    }

    @Override
    public TextView getCounter() {
        return mTextCounter;
    }

    @Override
    public IconTextView getSortTitle() {
        return mSortTitle;
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        setToolbarScrollFlags(false);
        ViewUtil.show(false, mSortPanel);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        setToolbarScrollFlags(true);
        ViewUtil.show(true, mSortPanel);
    }

    private void setToolbarScrollFlags(boolean scroll) {
        int flags = scroll ? SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS : 0;

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(flags);

        mAppBar.requestLayout();
    }

}

package com.axel_stein.noteapp.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.notes.list.NotesFragment;
import com.axel_stein.noteapp.notes.list.presenters.LabelNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotebookNotesPresenter;
import com.axel_stein.noteapp.utils.MenuUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NoteListActivity extends BaseActivity {

    private static final String EXTRA_NOTEBOOK_ID = "com.axel_stein.noteapp.main.EXTRA_NOTEBOOK_ID";
    private static final String EXTRA_NOTEBOOK_TITLE = "com.axel_stein.noteapp.main.EXTRA_NOTEBOOK_TITLE";

    private static final String EXTRA_LABEL_ID = "com.axel_stein.noteapp.main.EXTRA_LABEL_ID";
    private static final String EXTRA_LABEL_TITLE = "com.axel_stein.noteapp.main.EXTRA_LABEL_TITLE";

    public static void launch(Context context, Notebook notebook) {
        Intent intent = new Intent(context, NoteListActivity.class);
        intent.putExtra(EXTRA_NOTEBOOK_ID, notebook.getId());
        intent.putExtra(EXTRA_NOTEBOOK_TITLE, notebook.getTitle());
        context.startActivity(intent);
    }

    public static void launch(Context context, Label label) {
        Intent intent = new Intent(context, NoteListActivity.class);
        intent.putExtra(EXTRA_LABEL_ID, label.getId());
        intent.putExtra(EXTRA_LABEL_TITLE, label.getTitle());
        context.startActivity(intent);
    }

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab_add)
    FloatingActionButton mFAB;

    @Inject
    AppSettingsRepository mAppSettings;

    private Notebook mNotebook;
    private Label mLabel;

    @Nullable
    private NotesFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_NOTEBOOK_ID)) {
            mNotebook = new Notebook();
            mNotebook.setId(intent.getLongExtra(EXTRA_NOTEBOOK_ID, 0));
            mNotebook.setTitle(intent.getStringExtra(EXTRA_NOTEBOOK_TITLE));
        } else if (intent.hasExtra(EXTRA_LABEL_ID)) {
            mLabel = new Label();
            mLabel.setId(intent.getLongExtra(EXTRA_LABEL_ID, 0));
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

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("fragment");
        if (fragment == null) {
            mFragment = new NotesFragment();
            mFragment.showBottomPadding(true);

            int msg = 0;
            if (mNotebook != null) {
                msg = R.string.empty_notebook;
            } else if (mLabel != null) {
                msg = R.string.empty_label;
            }

            try {
                mFragment.setEmptyMsg(getString(msg));
            } catch (Exception ignored) {
            }

            if (mNotebook != null) {
                mFragment.setPresenter(new NotebookNotesPresenter(mNotebook));
            } else if (mLabel != null) {
                mFragment.setPresenter(new LabelNotesPresenter(mLabel));
            }

            setFragment(mFragment, "fragment");
        } else {
            mFragment = (NotesFragment) fragment;
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

                    Snackbar snackbar = Snackbar.make(mToolbar, msg, Snackbar.LENGTH_SHORT);
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

        MenuUtil.showGroup(menu,R.id.menu_group_notebook, mNotebook != null);
        MenuUtil.showGroup(menu,R.id.menu_group_label, mLabel != null);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        NoteOrder currentOrder = mAppSettings.getNotesOrder();
        if (currentOrder != null) {
            MenuItem item = menu.findItem(R.id.menu_sort);
            if (item != null) {
                MenuUtil.check(item.getSubMenu(), menuItemFromNoteOrder(currentOrder), true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NoteOrder order = noteOrderFromMenuItem(item);
        if (order != null) {
            item.setChecked(true);
            mAppSettings.setNotesOrder(order);
            EventBusHelper.updateNoteList();
            if (mFragment != null) {
                mFragment.scrollToTop();
            }
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_search:
                showSearchActivity();
                break;

            case R.id.menu_rename_notebook:
                renameNotebookDialog(mNotebook);
                break;

            case R.id.menu_delete_notebook:
                deleteNotebookDialog(mNotebook);
                break;

            case R.id.menu_rename_label:
                renameLabelDialog(mLabel);
                break;

            case R.id.menu_delete_label:
                deleteLabelDialog(mLabel);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private NoteOrder noteOrderFromMenuItem(MenuItem item) {
        if (item == null) {
            return null;
        }

        SparseArray<NoteOrder> sparseArray = new SparseArray<>();
        sparseArray.put(R.id.menu_sort_title, NoteOrder.TITLE);
        sparseArray.put(R.id.menu_sort_relevance, NoteOrder.RELEVANCE);
        sparseArray.put(R.id.menu_created_newest, NoteOrder.DATE_NEWEST);
        sparseArray.put(R.id.menu_created_oldest, NoteOrder.DATE_OLDEST);
        sparseArray.put(R.id.menu_updated_newest, NoteOrder.UPDATE_NEWEST);
        sparseArray.put(R.id.menu_updated_oldest, NoteOrder.UPDATE_OLDEST);

        return sparseArray.get(item.getItemId());
    }

    private int menuItemFromNoteOrder(NoteOrder order) {
        if (order == null) {
            return -1;
        }

        HashMap<NoteOrder, Integer> map = new HashMap<>();
        map.put(NoteOrder.TITLE, R.id.menu_sort_title);
        map.put(NoteOrder.RELEVANCE, R.id.menu_sort_relevance);
        map.put(NoteOrder.DATE_NEWEST, R.id.menu_created_newest);
        map.put(NoteOrder.DATE_OLDEST, R.id.menu_created_oldest);
        map.put(NoteOrder.UPDATE_NEWEST, R.id.menu_updated_newest);
        map.put(NoteOrder.UPDATE_OLDEST, R.id.menu_updated_oldest);

        return map.get(order);
    }

}

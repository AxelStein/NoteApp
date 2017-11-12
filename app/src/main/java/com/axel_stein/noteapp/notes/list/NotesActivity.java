package com.axel_stein.noteapp.notes.list;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.ResetInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.notes.list.presenters.LabelNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotebookNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotesPresenter;
import com.axel_stein.noteapp.utils.ColorUtil;
import com.axel_stein.noteapp.utils.DisplayUtil;
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;

public class NotesActivity extends BaseActivity implements ConfirmDialog.OnConfirmListener {

    private static final String TAG_RESET_PASSWORD = "TAG_RESET_PASSWORD";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.edit_password)
    EditText mEditPassword;

    @BindView(R.id.layout_password)
    View mLayoutPasswordInput;

    @BindView(R.id.fab_add_note)
    FloatingActionButton mAddNoteFAB;

    @Inject
    AppSettingsRepository mAppSettings;

    @Inject
    ResetInteractor mResetInteractor;

    private ActionBar mActionBar;

    private Drawer mDrawer;

    @Nullable
    private DrawerHelper mDrawerHelper;

    @Nullable
    private Object mTag;

    @Nullable
    private Menu mMenu;

    @Nullable
    private NotesFragment mFragment;

    private boolean mShowFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        App.getAppComponent().inject(this);
        ButterKnife.bind(this);
        EventBusHelper.subscribe(this);

        mShowFAB = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_fab", true);
        ViewUtil.show(mShowFAB, mAddNoteFAB);

        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("fragment");
        if (fragment == null) {
            mFragment = new NotesFragment();
            setFragment(mFragment, "fragment");
        } else {
            mFragment = (NotesFragment) fragment;
        }

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withStickyHeader(R.layout.layout_header)
                .withDelayDrawerClickEvent(300)
                .withSavedInstance(savedInstanceState)
                .build();
        mDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem item) {
                return drawerItemClick(item);
            }
        });

        setupDrawerHeader(mDrawer.getStickyHeader());

        findViewById(R.id.button_reset_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setTitle(R.string.title_password_reset);
                dialog.setMessage(R.string.msg_password_reset_caution);
                dialog.setPositiveButtonText(R.string.action_ok);
                dialog.setNegativeButtonText(R.string.action_cancel);
                dialog.show(getSupportFragmentManager(), TAG_RESET_PASSWORD);
            }
        });

        showPasswordLayout(mAppSettings.showPasswordInput());

        mEditPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkPassword();
            }
        });
        mEditPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_DONE && checkPassword();
            }
        });
        mEditPassword.post(new Runnable() {
            @Override
            public void run() {
                if (ViewUtil.isShown(mLayoutPasswordInput)) {
                    mEditPassword.requestFocus();
                    KeyboardUtil.show(mEditPassword);
                } else {
                    mLayoutPasswordInput.requestFocus();
                    KeyboardUtil.hide(mEditPassword);
                }
            }
        });

        if (savedInstanceState != null) {
            restoreCurrentItem(savedInstanceState);
        }
    }

    private boolean checkPassword() {
        if (mAppSettings.checkPassword(mEditPassword.getText().toString())) {
            showPasswordLayout(false);
            KeyboardUtil.hide(NotesActivity.this);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    private void setupDrawerHeader(View header) {
        header.setPadding(0, DisplayUtil.getStatusBarHeight(this), 0, 0);
        header.setBackgroundColor(ColorUtil.getColorAttr(this, R.attr.colorPrimary));

        header.findViewById(R.id.action_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingsActivity();
            }
        });
        header.findViewById(R.id.action_trash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTrashActivity();
            }
        });

        View nightMode = header.findViewById(R.id.action_night_mode);
        nightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAppSettings.setNightMode(!mAppSettings.nightMode());
                EventBusHelper.recreate();
            }
        });
    }

    private boolean drawerItemClick(final IDrawerItem item) {
        if (item == null) {
            return false;
        }

        switch ((int) item.getIdentifier()) {
            case R.id.menu_add_notebook:
                addNotebookDialog();
                return true;

            case R.id.menu_add_label:
                addLabelDialog();
                return true;
        }

        setTag(item.getTag(), true);

        return true;
    }

    private void setTag(Object tag, boolean updatePresenter) {
        mTag = tag;

        String title = null;
        String emptyMsg = null;
        NotesPresenter presenter = null;

        if (mTag instanceof Notebook) {
            Notebook notebook = (Notebook) mTag;

            title = notebook.getTitle();
            emptyMsg = getString(R.string.empty_notebook);
            presenter = new NotebookNotesPresenter(notebook);
        } else if (mTag instanceof Label) {
            Label label = (Label) mTag;

            title = label.getTitle();
            emptyMsg = getString(R.string.empty_label);
            presenter = new LabelNotesPresenter(label);
        }

        setActionBarTitle(title);
        if (mFragment != null) {
            mFragment.setEmptyMsg(emptyMsg);

            if (updatePresenter && presenter != null) {
                mFragment.setPresenter(presenter);
            }
        }

        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_notes, menu);
        mMenu = menu;

        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        MenuUtil.show(menu, !mShowFAB, R.id.menu_add_note);

        MenuUtil.show(mMenu, mTag != null, R.id.menu_sort);
        MenuUtil.showGroup(mMenu,R.id.menu_group_notebook, false);
        MenuUtil.showGroup(mMenu,R.id.menu_group_label, false);

        if (mDrawerHelper == null) {
            mDrawerHelper = new DrawerHelper(new DrawerHelper.Callback() {
                @Override
                public void update(List<IDrawerItem> items, boolean click, IDrawerItem selected) {
                    if (mDrawer != null) {
                        mDrawer.setItems(items);
                    }
                    if (click || mFragment != null && mFragment.getPresenter() == null) {
                        drawerItemClick(selected);
                    }
                    supportInvalidateOptionsMenu();
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        NoteOrder currentOrder = mAppSettings.getNotesOrder();
        if (currentOrder != null) {
            MenuItem item = menu.findItem(R.id.menu_sort);
            MenuUtil.check(item.getSubMenu(), menuItemFromNoteOrder(currentOrder), true);
        }

        if (mTag != null) {
            MenuUtil.showGroup(mMenu, R.id.menu_group_notebook, mTag instanceof Notebook);
            MenuUtil.showGroup(mMenu, R.id.menu_group_label, mTag instanceof Label);
        }

        MenuItem item = menu.findItem(R.id.menu_delete_notebook);
        if (item.isVisible()) {
            MenuUtil.show(item, mDrawerHelper != null && mDrawerHelper.getNotebookCount() > 1);
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
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_add_note:
                addNote();
                break;

            case R.id.menu_search:
                showSearchActivity();
                break;

            case R.id.menu_rename_notebook:
                if (mTag != null) {
                    renameNotebookDialog(getCurrentNotebook());
                }
                break;

            case R.id.menu_delete_notebook:
                if (mTag != null) {
                    deleteNotebookDialog(getCurrentNotebook());
                }
                break;

            case R.id.menu_rename_label:
                if (mTag != null) {
                    renameLabelDialog(getCurrentLabel());
                }
                break;

            case R.id.menu_delete_label:
                if (mTag != null) {
                    deleteLabelDialog(getCurrentLabel());
                }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveCurrentItem(outState);
    }

    private void restoreCurrentItem(Bundle saved) {
        long id = saved.getLong("id");
        String title = saved.getString("title");
        String type = saved.getString("type");

        if (type != null) {
            switch (type) {
                case "notebook":
                    Notebook notebook = new Notebook();
                    notebook.setId(id);
                    notebook.setTitle(title);
                    mTag = notebook;
                    break;

                case "label":
                    Label label = new Label();
                    label.setId(id);
                    label.setTitle(title);
                    mTag = label;
                    break;

                default:
                    return;
            }
        }

        setTag(mTag, false);
    }

    private void saveCurrentItem(Bundle outState) {
        if (mTag == null) {
            return;
        }

        long id;
        String title;

        if (mTag instanceof Notebook) {
            Notebook notebook = (Notebook) mTag;
            id = notebook.getId();
            title = notebook.getTitle();
            outState.putString("type", "notebook");
        } else if (mTag instanceof Label) {
            Label label = (Label) mTag;
            id = label.getId();
            title = label.getTitle();
            outState.putString("type", "label");
        } else {
            return;
        }

        outState.putLong("id", id);
        outState.putString("title", title);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.fab_add_note)
    public void addNote() {
        if (mTag instanceof Notebook) {
            EditNoteActivity.launch(this, (Notebook) mTag);
        } else if (mTag instanceof Label) {
            EditNoteActivity.launch(this, (Label) mTag);
        } else {
            EventBusHelper.showMessage(R.string.msg_create_notebook);
        }
    }

    private Notebook getCurrentNotebook() {
        if (mTag instanceof Notebook) {
            return (Notebook) mTag;
        }
        throw new IllegalStateException();
    }

    private Label getCurrentLabel() {
        if (mTag instanceof Label) {
            return (Label) mTag;
        }
        throw new IllegalStateException();
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

                    String actionName = e.getActionName();
                    if (e.hasActionNameRes()) {
                        actionName = getString(e.getActionNameRes());
                    }

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_notes), msg, Snackbar.LENGTH_SHORT);
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
    public void updateDrawer(EventBusHelper.UpdateDrawer e) {
        if (mDrawerHelper != null) {
            mDrawerHelper.update(e.saveSelection(), e.click());
            supportInvalidateOptionsMenu();
        }
    }

    @Subscribe
    public void addNotebook(EventBusHelper.AddNotebook e) {
        if (mDrawerHelper != null) {
            mDrawerHelper.addNotebook(e.getNotebook());
            supportInvalidateOptionsMenu();
        }
    }

    @Subscribe
    public void renameNotebook(EventBusHelper.RenameNotebook e) {
        if (mDrawerHelper != null) {
            mDrawerHelper.renameNotebook(e.getNotebook());
        }
    }

    @Subscribe
    public void deleteNotebook(EventBusHelper.DeleteNotebook e) {
        if (mDrawerHelper != null) {
            mDrawerHelper.deleteNotebook(e.getNotebook());
            supportInvalidateOptionsMenu();
        }
    }

    @Subscribe
    public void addLabel(EventBusHelper.AddLabel e) {
        if (mDrawerHelper != null) {
            mDrawerHelper.addLabel(null);
            supportInvalidateOptionsMenu();
        }
    }

    @Subscribe
    public void renameLabel(EventBusHelper.RenameLabel e) {
        if (mDrawerHelper != null) {
            mDrawerHelper.renameLabel(null);
        }
    }

    @Subscribe
    public void deleteLabel(EventBusHelper.DeleteLabel e) {
        if (mDrawerHelper != null) {
            mDrawerHelper.deleteLabel(null);
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        if (mShowFAB) {
            ViewUtil.show(false, mAddNoteFAB);
        }
        lockDrawer(true);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        if (mShowFAB) {
            ViewUtil.show(true, mAddNoteFAB);
        }
        lockDrawer(false);
        super.onSupportActionModeFinished(mode);
    }

    private void lockDrawer(boolean lock) {
        if (mDrawer != null) {
            DrawerLayout layout = mDrawer.getDrawerLayout();
            if (layout != null) {
                layout.setDrawerLockMode(lock ? LOCK_MODE_LOCKED_CLOSED : LOCK_MODE_UNLOCKED);
            }
        }
    }

    private void setActionBarTitle(final String title) {
        if (mActionBar != null) {
            mActionBar.setTitle(title);
        }
    }

    @Override
    public void onConfirm(String tag) {
        if (tag != null) {
            switch (tag) {
                case TAG_RESET_PASSWORD:
                    mAppSettings.setPassword(null);
                    mResetInteractor.execute()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onComplete() {
                                    showPasswordLayout(false);
                                    EventBusHelper.recreate();
                                    EventBusHelper.updateNoteList(false, true);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                    EventBusHelper.showMessage(R.string.error);
                                }
                            });
                    break;
            }
        }
    }

    @Override
    public void onCancel(String tag) {

    }

    private void showPasswordLayout(boolean show) {
        ViewUtil.show(show, mLayoutPasswordInput);
        lockDrawer(show);
    }

}

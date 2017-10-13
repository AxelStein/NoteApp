package com.axel_stein.noteapp.notes.list;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.view.ActionMode;
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
import com.axel_stein.noteapp.notes.list.presenters.LabelNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotebookNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotesPresenter;
import com.axel_stein.noteapp.utils.ColorUtil;
import com.axel_stein.noteapp.utils.DisplayUtil;
import com.axel_stein.noteapp.utils.MenuUtil;
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

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;

public class NotesActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab_add_note)
    FloatingActionButton mAddNoteFAB;
    @Inject
    AppSettingsRepository mAppSettings;
    private ActionBar mActionBar;
    private Drawer mDrawer;
    private DrawerHelper mDrawerHelper;
    private Object mTag;
    private Menu mMenu;
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

        if (savedInstanceState != null) {
            restoreCurrentItem(savedInstanceState);
        }
    }

    /*
    10-16 13:19:45.286 194-389/? E/EmbeddedLogger: App crashed! Process: com.axel_stein.noteapp
10-16 13:19:45.286 194-389/? E/EmbeddedLogger: App crashed! Package: com.axel_stein.noteapp v1 (1.0)
10-16 13:19:45.286 3242-3242/com.axel_stein.noteapp E/AndroidRuntime: FATAL EXCEPTION: main
                                                                      java.lang.RuntimeException: Unable to start activity ComponentInfo{com.axel_stein.noteapp/com.axel_stein.noteapp.notes.list.NotesActivity}: android.view.InflateException: Binary XML file line #7: Error inflating class ImageButton
                                                                          at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2205)
                                                                          at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2240)
                                                                          at android.app.ActivityThread.access$600(ActivityThread.java:139)
                                                                          at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1262)
                                                                          at android.os.Handler.dispatchMessage(Handler.java:99)
                                                                          at android.os.Looper.loop(Looper.java:156)
                                                                          at android.app.ActivityThread.main(ActivityThread.java:4987)
                                                                          at java.lang.reflect.Method.invokeNative(Native Method)
                                                                          at java.lang.reflect.Method.invoke(Method.java:511)
                                                                          at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:784)
                                                                          at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:551)
                                                                          at dalvik.system.NativeStart.main(Native Method)
                                                                       Caused by: android.view.InflateException: Binary XML file line #7: Error inflating class ImageButton
                                                                          at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:697)
                                                                          at android.view.LayoutInflater.rInflate(LayoutInflater.java:739)
                                                                          at android.view.LayoutInflater.inflate(LayoutInflater.java:489)
                                                                          at android.view.LayoutInflater.inflate(LayoutInflater.java:396)
                                                                          at com.mikepenz.materialdrawer.DrawerBuilder.withStickyHeader(DrawerBuilder.java:638)
                                                                          at com.axel_stein.noteapp.notes.list.NotesActivity.onCreate(NotesActivity.java:98)
                                                                          at android.app.Activity.performCreate(Activity.java:4538)
                                                                          at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1071)
                                                                          at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2161)
                                                                          at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2240) 
                                                                          at android.app.ActivityThread.access$600(ActivityThread.java:139) 
                                                                          at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1262) 
                                                                          at android.os.Handler.dispatchMessage(Handler.java:99) 
                                                                          at android.os.Looper.loop(Looper.java:156) 
                                                                          at android.app.ActivityThread.main(ActivityThread.java:4987) 
                                                                          at java.lang.reflect.Method.invokeNative(Native Method) 
                                                                          at java.lang.reflect.Method.invoke(Method.java:511) 
                                                                          at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:784) 
                                                                          at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:551) 
                                                                          at dalvik.system.NativeStart.main(Native Method) 
                                                                       Caused by: java.lang.NumberFormatException: Invalid int: "res/color/abc_secondary_text_material_light.xml"
                                                                          at java.lang.Integer.invalidInt(Integer.java:138)
                                                                          at java.lang.Integer.parse(Integer.java:375)
                                                                          at java.lang.Integer.parseInt(Integer.java:366)
                                                                          at com.android.internal.util.XmlUtils.convertValueToInt(XmlUtils.java:123)
                                                                          at android.content.res.TypedArray.getInt(TypedArray.java:254)
                                                                          at android.widget.ImageView.<init>(ImageView.java:145)
                                                                          at android.widget.ImageButton.<init>(ImageButton.java:85)
                                                                          at android.support.v7.widget.AppCompatImageButton.<init>(AppCompatImageButton.java:73)
                                                                          at android.support.v7.widget.AppCompatImageButton.<init>(AppCompatImageButton.java:69)
                                                                          at android.support.v7.app.AppCompatViewInflater.createView(AppCompatViewInflater.java:118)
                                                                          at android.support.v7.app.AppCompatDelegateImplV9.createView(AppCompatDelegateImplV9.java:1024)
                                                                          at android.support.v7.app.AppCompatDelegateImplV9.onCreateView(AppCompatDelegateImplV9.java:1081)
                                                                          at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:668)
                                                                          at android.view.LayoutInflater.rInflate(LayoutInflater.java:739) 
                                                                          at android.view.LayoutInflater.inflate(LayoutInflater.java:489) 
                                                                          at android.view.LayoutInflater.inflate(LayoutInflater.java:396) 
                                                                          at com.mikepenz.materialdrawer.DrawerBuilder.withStickyHeader(DrawerBuilder.java:638) 
                                                                          at com.axel_stein.noteapp.notes.list.NotesActivity.onCreate(NotesActivity.java:98) 
                                                                          at android.app.Activity.performCreate(Activity.java:4538) 
                                                                          at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1071) 
                                                                          at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2161) 
                                                                          at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2240) 
                                                                          at android.app.ActivityThread.access$600(ActivityThread.java:139) 
                                                                          at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1262) 
                                                                          at android.os.Handler.dispatchMessage(Handler.java:99) 
                                                                          at android.os.Looper.loop(Looper.java:156) 
                                                                          at android.app.ActivityThread.main(ActivityThread.java:4987) 
                                                                          at java.lang.reflect.Method.invokeNative(Native Method) 
                                                                          at java.lang.reflect.Method.invoke(Method.java:511) 
                                                                          at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:784) 
                                                                          at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:551) 
                                                                          at dalvik.system.NativeStart.main(Native Method) 
10-16 13:19:45.296 194-389/? E/EmbeddedLogger: Application Label: NoteApp
    */

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

    private boolean drawerItemClick(IDrawerItem item) {
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
        mFragment.setEmptyMsg(emptyMsg);
        if (updatePresenter && presenter != null) {
            mFragment.setPresenter(presenter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_notes, menu);
        mMenu = menu;

        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        MenuUtil.showMenuItem(menu, !mShowFAB, R.id.menu_add_note);

        if (mDrawerHelper == null) {
            mDrawerHelper = new DrawerHelper(new DrawerHelper.Callback() {
                @Override
                public void update(List<IDrawerItem> items, boolean click, IDrawerItem selected) {
                    mDrawer.setItems(items);
                    if (click || mFragment.getPresenter() == null) {
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
            MenuUtil.checkMenuItem(item.getSubMenu(), menuItemFromNoteOrder(currentOrder), true);
        }

        if (mTag != null) {
            MenuUtil.showGroup(mMenu, R.id.menu_group_notebook, mTag instanceof Notebook);
            MenuUtil.showGroup(mMenu, R.id.menu_group_label, mTag instanceof Label);
        }

        MenuItem item = menu.findItem(R.id.menu_delete_notebook);
        if (item.isVisible()) {
            MenuUtil.showMenuItem(item, mDrawerHelper.getNotebookCount() > 1);
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
                renameNotebookDialog(getCurrentNotebook());
                break;

            case R.id.menu_delete_notebook:
                deleteNotebookDialog(getCurrentNotebook());
                break;

            case R.id.menu_rename_label:
                renameLabelDialog(getCurrentLabel());
                break;

            case R.id.menu_delete_label:
                deleteLabelDialog(getCurrentLabel());
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
        if (mDrawer.isDrawerOpen()) {
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
    public void showMessage(EventBusHelper.Message e) {
        if (e.isRes()) {
            showMessage(e.getMsgRes());
        } else {
            showMessage(e.getMsg());
        }
    }

    @Subscribe
    public void onRecreate(EventBusHelper.Recreate e) {
        // todo bug with API 15
        recreate();
    }

    /*
    @Subscribe
    public void updateNotebookCounters(EventBusHelper.UpdateNotebookCounters e) {
        mDrawerHelper.updateNotebookCounters();
    }
    */

    @Subscribe
    public void updateDrawer(EventBusHelper.UpdateDrawer e) {
        mDrawerHelper.update(true, false);
        supportInvalidateOptionsMenu();
    }

    @Subscribe
    public void addNotebook(EventBusHelper.AddNotebook e) {
        mDrawerHelper.addNotebook(e.getNotebook());
        supportInvalidateOptionsMenu();
    }

    @Subscribe
    public void renameNotebook(EventBusHelper.RenameNotebook e) {
        mDrawerHelper.renameNotebook(e.getNotebook());
    }

    @Subscribe
    public void deleteNotebook(EventBusHelper.DeleteNotebook e) {
        mDrawerHelper.deleteNotebook(e.getNotebook());
        supportInvalidateOptionsMenu();
    }

    @Subscribe
    public void addLabel(EventBusHelper.AddLabel e) {
        mDrawerHelper.addLabel(null);
        supportInvalidateOptionsMenu();
    }

    @Subscribe
    public void renameLabel(EventBusHelper.RenameLabel e) {
        mDrawerHelper.renameLabel(null);
    }

    @Subscribe
    public void deleteLabel(EventBusHelper.DeleteLabel e) {
        mDrawerHelper.deleteLabel(null);
        supportInvalidateOptionsMenu();
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

    private void showMessage(final String msg) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Snackbar.make(findViewById(R.id.coordinator_notes), msg, Snackbar.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                }
            }
        }, 100);
    }

    private void showMessage(int msgRes) {
        showMessage(getString(msgRes));
    }

    private void lockDrawer(boolean lock) {
        mDrawer.getDrawerLayout().setDrawerLockMode(lock ? LOCK_MODE_LOCKED_CLOSED : LOCK_MODE_UNLOCKED);
    }

    private void setActionBarTitle(String title) {
        if (mActionBar != null) {
            mActionBar.setTitle(title);
        }
    }

}

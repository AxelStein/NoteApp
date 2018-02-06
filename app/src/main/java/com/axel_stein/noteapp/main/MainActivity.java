package com.axel_stein.noteapp.main;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.label.AddLabelDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.utils.BottomNavigationViewHelper;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigation;

    @BindView(R.id.fab_add)
    FloatingActionButton mFAB;

    private static final String TAG_SHOW_FAB = "TAG_SHOW_FAB";

    @Inject
    AppSettingsRepository mAppSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getAppComponent().inject(this);
        ButterKnife.bind(this);
        EventBusHelper.subscribe(this);

        setSupportActionBar(mToolbar);

        boolean nightMode = mAppSettings.nightMode();
        ColorStateList iconTint = ContextCompat.getColorStateList(this,
                nightMode ? R.color.bottom_navigation_icon_dark : R.color.bottom_navigation_icon_light);
        ColorStateList textColor = ContextCompat.getColorStateList(this,
                nightMode ? R.color.bottom_navigation_text_dark : R.color.bottom_navigation_text_light);

        mBottomNavigation.setItemIconTintList(iconTint);
        mBottomNavigation.setItemTextColor(textColor);

        BottomNavigationViewHelper.disableShiftMode(mBottomNavigation);

        mBottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home: {
                        setFragment(new HomeFragment(), "");
                        break;
                    }

                    case R.id.action_notebooks:
                        setFragment(new NotebooksFragment(), "");
                        break;

                    case R.id.action_labels:
                        setFragment(new LabelsFragment(), "");
                        break;

                    case R.id.action_trash:
                        setFragment(new TrashFragment(), "");
                        mFAB.hide();
                        return true;
                }

                mFAB.show();

                return true;
            }
        });

        if (savedInstanceState == null) {
            mBottomNavigation.setSelectedItemId(R.id.action_home);
        }

        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mBottomNavigation.getSelectedItemId()) {
                    case R.id.action_home:
                        EditNoteActivity.launch(MainActivity.this);
                        break;

                    case R.id.action_notebooks:
                        AddNotebookDialog.launch(MainActivity.this);
                        break;

                    case R.id.action_labels:
                        AddLabelDialog.launch(MainActivity.this);
                        break;
                }
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                showSearchActivity();
                break;

            case R.id.menu_settings:
                showSettingsActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean(TAG_SHOW_FAB, ViewUtil.isShown(mFAB));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            ViewUtil.show(savedInstanceState.getBoolean(TAG_SHOW_FAB, true), mFAB);
        }
    }

}

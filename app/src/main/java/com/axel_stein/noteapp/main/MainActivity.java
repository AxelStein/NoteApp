package com.axel_stein.noteapp.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.EventBusHelper.SignOutEvent;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.ScrollableFragment;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.label.AddLabelDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.google_drive.GoogleDriveInteractor;
import com.axel_stein.noteapp.google_drive.OnSignInListener;
import com.axel_stein.noteapp.google_drive.UserData;
import com.axel_stein.noteapp.label_manager.LabelManagerFragment;
import com.axel_stein.noteapp.notebook_manager.NotebookManagerFragment;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.notes.list.NotesFragment;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.BottomMenuView;
import com.axel_stein.noteapp.views.IconTextView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;

public class MainActivity extends BaseActivity implements SortPanelListener {

    private static final int REQUEST_CODE_SIGN_IN = 100;
    private static final String TAG_FRAGMENT = "TAG_FRAGMENT";
    private static final String TAG_SHOW_FAB = "TAG_SHOW_FAB";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.app_bar)
    AppBarLayout mAppBar;

    @BindView(R.id.bottom_navigation)
    BottomMenuView mBottomNavigation;

    @BindView(R.id.fab_add)
    FloatingActionButton mFAB;

    @BindView(R.id.user_photo)
    ImageView mUserPhoto;

    @BindView(R.id.user_name)
    TextView mUserName;

    @BindView(R.id.user_panel)
    View mUserPanel;

    @BindView(R.id.user_loading_indicator)
    View mUserLoadingIndicator;

    @BindView(R.id.sort_panel)
    View mSortPanel;

    @BindView(R.id.text_item_counter)
    TextView mTextCounter;

    @BindView(R.id.text_sort)
    IconTextView mSortTitle;

    @Inject
    AppSettingsRepository mAppSettings;

    @Inject
    GoogleDriveInteractor mGoogleDrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getAppComponent().inject(this);
        ButterKnife.bind(this);
        EventBusHelper.subscribe(this);

        setSupportActionBar(mToolbar);
        /*
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        */

        boolean nightMode = mAppSettings.nightMode();
        mBottomNavigation.setItemIconTintList(ContextCompat.getColorStateList(this,
                nightMode ? R.color.bottom_navigation_icon_dark : R.color.bottom_navigation_icon_light));
        mBottomNavigation.setItemTextColor(ContextCompat.getColorStateList(this,
                nightMode ? R.color.bottom_navigation_text_dark : R.color.bottom_navigation_text_light));

        mBottomNavigation.setItemSelectedListener(new BottomMenuView.OnNavigationItemSelectedListener() {
            @Override
            public void onNavigationItemSelected(int itemId) {
                mAppBar.setExpanded(true, true);

                handleBottomMenuClick(itemId);
            }
        });
        mBottomNavigation.setItemReselectedListener(new BottomMenuView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(int itemId) {
                mAppBar.setExpanded(true, true);

                if (!hasFragment(TAG_FRAGMENT)) {
                    handleBottomMenuClick(itemId);
                } else {
                    Fragment f = findMainFragment();
                    if (f != null && f instanceof ScrollableFragment) {
                        ((ScrollableFragment) f).scrollToTop();
                    }
                }
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

        mUserPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mGoogleDrive.isSignedIn()) {
                    startActivityForResult(mGoogleDrive.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                } else {
                    startActivity(new Intent(MainActivity.this, UserActivity.class));
                }
            }
        });

        ViewUtil.hide(mUserPanel);
        //setUserData(mGoogleDrive.isSignedIn() ? mGoogleDrive.getUserData() : null);
    }

    private void setUserData(UserData user) {
        Uri photo = null;
        String name = null;

        if (user != null) {
            photo = user.getPhoto();
            name = user.getName();
        }

        boolean nightMode = mAppSettings.nightMode();
        int icon = nightMode ? R.drawable.ic_account_circle_white_36dp : R.drawable.ic_account_circle_grey_36dp;

        if (photo == null) {
            mUserPhoto.setImageResource(icon);
        } else {
            Picasso.get().load(photo).placeholder(icon).into(mUserPhoto);
        }

        if (name == null) {
            ViewUtil.setText(mUserName, R.string.guest);
        } else {
            ViewUtil.setText(mUserName, name);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    mGoogleDrive.signIn(new OnSignInListener() {
                        @Override
                        public void onSuccess(UserData user) {
                            setUserData(user);
                        }
                    });
                }
                break;
        }
    }

    @Subscribe
    public void onSignOut(SignOutEvent e) {
        setUserData(null);
    }

    @Nullable
    private Fragment findMainFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentByTag(TAG_FRAGMENT);
    }

    private void handleBottomMenuClick(int id) {
        Fragment f = findMainFragment();
        if (f != null && f instanceof NotesFragment) {
            ((NotesFragment) f).stopCheckMode();
        }

        Fragment fragment = null;
        boolean showFAB = true;

        switch (id) {
            case R.id.action_home: {
                fragment = new InboxFragment();
                break;
            }

            case R.id.action_notebooks:
                fragment = new NotebookManagerFragment();
                break;

            case R.id.action_labels:
                fragment = new LabelManagerFragment();
                break;

            case R.id.action_trash:
                fragment = new TrashFragment();
                showFAB = false;
                break;
        }

        if (showFAB) {
            mFAB.show();
        } else {
            mFAB.hide();
        }

        setFragment(fragment, TAG_FRAGMENT);
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

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_main), msg, Snackbar.LENGTH_SHORT);
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
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        setToolbarScrollFlags(false);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        setToolbarScrollFlags(true);
    }

    private void setToolbarScrollFlags(boolean scroll) {
        int flags = scroll ? SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS : 0;

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(flags);

        mAppBar.requestLayout();
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

}

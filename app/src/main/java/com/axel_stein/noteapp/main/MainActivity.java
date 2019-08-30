package com.axel_stein.noteapp.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.main_menu.DividerItem;
import com.axel_stein.noteapp.dialogs.main_menu.MainMenuDialog;
import com.axel_stein.noteapp.dialogs.main_menu.PrimaryItem;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.google_drive.DriveServiceHelper;
import com.axel_stein.noteapp.main.edit.EditNoteActivity;
import com.axel_stein.noteapp.main.list.SearchActivity;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity implements MainMenuDialog.OnMenuItemClickListener, OnTitleChangeListener {
    private static final int REQUEST_CODE_SIGN_IN = 1;

    private static final String TAG_MAIN_MENU = "TAG_MAIN_MENU";
    private static final String TAG_FRAGMENT = "TAG_FRAGMENT";
    private static final String ID_INBOX = "ID_INBOX";
    private static final String ID_STARRED = "ID_STARRED";
    private static final String ID_TRASH = "ID_TRASH";
    private static final String ID_ADD_NOTEBOOK = "ID_ADD_NOTEBOOK";
    private static final String BUNDLE_SELECTED_ITEM_ID = "BUNDLE_SELECTED_ITEM_ID";

    @Inject
    AppSettingsRepository mAppSettings;

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Inject
    DriveServiceHelper mDriveServiceHelper;

    @Inject
    ImportBackupInteractor mImportBackupInteractor;

    private BottomAppBar mAppBar;
    private FloatingActionButton mFabCreateNote;
    private TextView mTextViewTitle;
    private String mSelectedItemId = ID_INBOX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppBar = findViewById(R.id.app_bar);
        setSupportActionBar(mAppBar);

        mTextViewTitle = findViewById(R.id.text_title);

        mFabCreateNote = findViewById(R.id.fab_create_note);
        mFabCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = "";
                switch (mSelectedItemId) {
                    case ID_INBOX: break;
                    case ID_STARRED: break;
                    case ID_TRASH: break;
                    default: id = mSelectedItemId;
                }
                EditNoteActivity.launch(MainActivity.this, id);
            }
        });

        App.getAppComponent().inject(this);
        EventBusHelper.subscribe(this);

        if (savedInstanceState == null) {
            onMenuItemClick(mSelectedItemId, true);
        }
    }

    @SuppressLint("CheckResult")
    private void openMainMenu() {
        mQueryNotebookInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Notebook>>() {
                    @Override
                    public void accept(List<Notebook> notebooks) {
                        MainMenuDialog.Builder builder = new MainMenuDialog.Builder();
                        builder.setSelectedItemId(mSelectedItemId);
                        builder.addItem(new PrimaryItem()
                                .fromId(ID_INBOX)
                                .fromTitle(R.string.action_inbox)
                                .fromIcon(R.drawable.ic_inbox));
                        builder.addItem(new PrimaryItem()
                                .fromId(ID_STARRED)
                                .fromTitle(R.string.action_starred)
                                .fromIcon(R.drawable.ic_star_border));

                        for (Notebook notebook : notebooks) {
                            builder.addItem(new PrimaryItem()
                                    .fromId(notebook.getId())
                                    .fromTitle(notebook.getTitle())
                                    .fromIcon(R.drawable.ic_book));
                        }

                        //builder.addItem(new DividerItem());
                        builder.addItem(new PrimaryItem()
                                .fromId(ID_ADD_NOTEBOOK)
                                .fromTitle(R.string.action_add_notebook)
                                .fromIcon(R.drawable.ic_add_box)
                                .fromCheckable(false));
                        builder.addItem(new DividerItem());

                        builder.addItem(new PrimaryItem()
                                .fromId(ID_TRASH)
                                .fromTitle(R.string.action_trash)
                                .fromIcon(R.drawable.ic_delete));

                        builder.show(MainActivity.this, TAG_MAIN_MENU);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
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

    @Subscribe
    public void onNotebookAdded(EventBusHelper.AddNotebook e) {
        Notebook notebook = e.getNotebook();
        onMenuItemClick(notebook.getId(), true);
    }

    @Subscribe
    public void onNotebookDeleted(EventBusHelper.DeleteNotebook e) {
        onMenuItemClick(ID_INBOX, true);
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
            case android.R.id.home:
                openMainMenu();
                return true;

            case R.id.menu_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putString(BUNDLE_SELECTED_ITEM_ID, mSelectedItemId);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedItemId = savedInstanceState.getString(BUNDLE_SELECTED_ITEM_ID);
        }
    }

    @Override
    public void onMenuItemClick(MainMenuDialog dialog, PrimaryItem item) {
        onMenuItemClick(item.getId(), item.isSelectable());
        dialog.dismiss();
    }

    private void onMenuItemClick(String itemId, boolean selectable) {
        if (selectable) {
            mSelectedItemId = itemId;
        }

        switch (itemId) {
            case ID_INBOX:
                setFragment(new InboxFragment(), TAG_FRAGMENT);
                break;

            case ID_STARRED:
                setFragment(new StarredFragment(), TAG_FRAGMENT);
                break;

            case ID_ADD_NOTEBOOK:
                AddNotebookDialog.launch(this);
                break;

            case ID_TRASH:
                setFragment(new TrashFragment(), TAG_FRAGMENT);
                break;

            default:
                NotebookNotesFragment fragment = new NotebookNotesFragment();
                fragment.setNotebookId(itemId);
                setFragment(fragment, TAG_FRAGMENT);
                break;
        }
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        ViewUtil.hide(mAppBar, mFabCreateNote);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        ViewUtil.show(mAppBar, mFabCreateNote);
    }

    @Override
    public void onTitleChange(String title) {
        if (mTextViewTitle != null) {
            mTextViewTitle.setText(title);
        }
    }

    @Override
    public void onUserPanelClick(MainMenuDialog dialog) {
        if (!mDriveServiceHelper.isSignedIn()) {
            startActivityForResult(mDriveServiceHelper.requestSignInIntent(), REQUEST_CODE_SIGN_IN);
        } else {
            startActivity(new Intent(this, UserActivity.class));
        }
        dialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                handleSignInResult(resultData);
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleAccount) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        showMessage(new EventBusHelper.Message("Unable to sign in.))"));
                        Log.e("TAG", "Unable to sign in.", exception);
                    }
                });
    }


}

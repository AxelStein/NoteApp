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
import androidx.fragment.app.FragmentManager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.main_menu.MainMenuDialog;
import com.axel_stein.noteapp.dialogs.main_menu.PrimaryItem;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.google_drive.DriveServiceHelper;
import com.axel_stein.noteapp.google_drive.DriveWorker;
import com.axel_stein.noteapp.main.edit.EditNoteActivity;
import com.axel_stein.noteapp.main.list.SearchActivity;
import com.axel_stein.noteapp.settings.SettingsActivity;
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

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.data.AppSettingsRepository.BACKUP_FILE_NAME;
import static com.axel_stein.noteapp.dialogs.main_menu.MainMenuDialog.ID_ADD_NOTEBOOK;
import static com.axel_stein.noteapp.dialogs.main_menu.MainMenuDialog.ID_INBOX;
import static com.axel_stein.noteapp.dialogs.main_menu.MainMenuDialog.ID_REMINDERS;
import static com.axel_stein.noteapp.dialogs.main_menu.MainMenuDialog.ID_STARRED;
import static com.axel_stein.noteapp.dialogs.main_menu.MainMenuDialog.ID_TRASH;

public class MainActivity extends BaseActivity implements MainMenuDialog.OnMenuItemClickListener, OnTitleChangeListener {
    private static final int REQUEST_CODE_SIGN_IN = 1;

    private static final String TAG_MAIN_MENU = "TAG_MAIN_MENU";
    private static final String TAG_FRAGMENT = "TAG_FRAGMENT";
    private static final String BUNDLE_SELECTED_ITEM_ID = "BUNDLE_SELECTED_ITEM_ID";
    private static final String BUNDLE_TITLE = "BUNDLE_TITLE";
    private static final String BUNDLE_FRAGMENT = "BUNDLE_FRAGMENT";
    private static final String BUNDLE_MAIN_MENU_FRAGMENT = "BUNDLE_MAIN_MENU_FRAGMENT";

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Inject
    DriveServiceHelper mDriveServiceHelper;

    @Inject
    ImportBackupInteractor mImportBackupInteractor;

    @Inject
    AppSettingsRepository mSettings;

    private BottomAppBar mAppBar;
    private FloatingActionButton mFabCreateNote;
    private TextView mTextViewTitle;
    private String mSelectedItemId = ID_INBOX;
    private boolean mStopped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        EventBusHelper.subscribe(this);
        setContentView(R.layout.activity_main);

        if (mSettings.autoSyncEnabled()) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            WorkRequest request = new OneTimeWorkRequest.Builder(DriveWorker.class).setConstraints(constraints).build();
            WorkManager.getInstance(this).enqueue(request);
        }

        mAppBar = findViewById(R.id.app_bar);
        setSupportActionBar(mAppBar);

        mTextViewTitle = findViewById(R.id.text_title);

        mFabCreateNote = findViewById(R.id.fab_create_note);
        mFabCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = "";
                switch (mSelectedItemId) {
                    case ID_INBOX:
                    case ID_STARRED:
                    case ID_TRASH:
                    case ID_REMINDERS:
                        break;
                    default: id = mSelectedItemId;
                }
                EditNoteActivity.launch(MainActivity.this, id);
            }
        });

        if (savedInstanceState == null) {
            onMenuItemClick(mSelectedItemId, true);
        } else {
            mSelectedItemId = savedInstanceState.getString(BUNDLE_SELECTED_ITEM_ID);
        }
    }

    @Override
    protected void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @SuppressLint("CheckResult")
    private void openMainMenu() {
        MainMenuDialog.launch(getSupportFragmentManager(), TAG_MAIN_MENU, mSelectedItemId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStopped = false;
    }

    @Override
    protected void onStop() {
        mStopped = true;
        super.onStop();
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
    public void onImportCompleted(EventBusHelper.ImportCompleted e) {
        onMenuItemClick(ID_INBOX, true); // fixme not working
    }

    @Subscribe
    public void onNotebookAdded(final EventBusHelper.AddNotebook e) {
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_SELECTED_ITEM_ID, mSelectedItemId);
        outState.putString(BUNDLE_TITLE, mTextViewTitle.getText().toString());

        FragmentManager fm = getSupportFragmentManager();
        if (getFragment(TAG_FRAGMENT) != null) {
            fm.putFragment(outState, BUNDLE_FRAGMENT, getFragment(TAG_FRAGMENT));
        }
        if (getFragment(TAG_MAIN_MENU) != null) {
            fm.putFragment(outState, BUNDLE_MAIN_MENU_FRAGMENT, getFragment(TAG_MAIN_MENU));
        }
    }

    @Override
    public void onMenuItemClick(MainMenuDialog dialog, PrimaryItem item) {
        onMenuItemClick(item.getId(), item.isSelectable());
        dialog.dismiss();
    }

    private void onMenuItemClick(String itemId, boolean selectable) {
        if (mStopped) {
            return;
        }

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

            case ID_REMINDERS:
                setFragment(new RemindersFragment(), TAG_FRAGMENT);
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
    public void onSettingsClick(MainMenuDialog dialog) {
        dialog.dismiss();
        startActivity(new Intent(this, SettingsActivity.class));
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
                        importDrive();
                        //startActivity(new Intent(MainActivity.this, UserActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                        showMessage(new EventBusHelper.Message("Unable to sign in.))"));
                        Log.e("TAG", "Unable to sign in.", exception);
                    }
                });
    }

    private void importDrive() {
        mDriveServiceHelper.downloadFile(BACKUP_FILE_NAME, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                mImportBackupInteractor.execute(s)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {}

                            @Override
                            public void onComplete() {
                                EventBusHelper.updateNoteList();
                                EventBusHelper.recreate();
                                EventBusHelper.importCompleted();
                                EventBusHelper.showMessage(R.string.msg_import_success);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                EventBusHelper.showMessage(R.string.error);
                            }
                        });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                EventBusHelper.showMessage(R.string.error);
            }
        });
    }


}

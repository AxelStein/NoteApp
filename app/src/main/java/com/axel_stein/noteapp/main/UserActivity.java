package com.axel_stein.noteapp.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.google_drive.DriveServiceHelper;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.axel_stein.data.AppSettingsRepository.BACKUP_FILE_NAME;
import static com.axel_stein.domain.utils.TextUtil.notEmpty;
import static com.axel_stein.noteapp.utils.FileUtil.writeToFile;

public class UserActivity extends BaseActivity {

    @Inject
    CreateBackupInteractor mCreateBackupInteractor;

    @Inject
    ImportBackupInteractor mImportBackupInteractor;

    @Inject
    DriveServiceHelper mDriveServiceHelper;

    private TextView mTextLastSync;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        EventBusHelper.subscribe(this);
        setContentView(R.layout.activity_user);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        TextView mTextUserName = findViewById(R.id.text_user_name);
        TextView mTextUserEmail = findViewById(R.id.text_user_email);
        CircleImageView mUserPhoto = findViewById(R.id.user_photo);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            ViewUtil.show(mTextUserName, mTextUserEmail);

            mTextUserName.setText(account.getDisplayName());
            mTextUserEmail.setText(account.getEmail());

            Picasso.get().load(account.getPhotoUrl()).placeholder(R.drawable.ic_account_circle_36).into(mUserPhoto);
        } else {
            ViewUtil.hide(mTextUserName, mTextUserEmail);
        }

        findViewById(R.id.text_import_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importDrive();
            }
        });

        findViewById(R.id.text_export_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo ConfirmDialog.from(R.string.title_export, 0, R.string.action_export, R.string.action_cancel).show();
                exportDrive();
            }
        });

        mTextLastSync = findViewById(R.id.text_last_sync);
        ViewUtil.hide(mTextLastSync);

        updateLastSyncTime();
    }

    @Subscribe
    public void onRecreate(EventBusHelper.Recreate e) {
        finish();
    }

    @SuppressLint("CheckResult")
    private void exportDrive() {
        mCreateBackupInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String backup) {
                        File file = writeToFile(getFilesDir(), BACKUP_FILE_NAME, backup);
                        mDriveServiceHelper.uploadBackup(file, new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                updateLastSyncTime();
                                EventBusHelper.showMessage(R.string.msg_export_success);
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    private void importDrive() {
        mDriveServiceHelper.downloadBackup(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                mImportBackupInteractor.execute(s)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                EventBusHelper.updateNoteList();
                                EventBusHelper.recreate();
                                EventBusHelper.showMessage(R.string.msg_import_success);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        });
            }
        });
    }

    private void updateLastSyncTime() {
        mDriveServiceHelper.getModifiedDate(this, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                ViewUtil.setText(mTextLastSync, getString(R.string.last_synced) + " " + s);
                ViewUtil.show(notEmpty(s), mTextLastSync);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user, menu);
        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            mDriveServiceHelper.signOut(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    finish();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void showMessage(EventBusHelper.Message e) {
        if (e.hasMsgRes()) {
            showMessage(e.getMsgRes(), e.getDelay());
        } else {
            showMessage(e.getMsg(), e.getDelay());
        }
    }

    private void showMessage(int msgRes, int delay) {
        showMessage(getString(msgRes), delay);
    }

    private void showMessage(final String msg, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Snackbar.make(findViewById(R.id.coordinator_user), msg, Snackbar.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                }
            }
        }, delay == 0 ? 100 : delay);
    }

    @Override
    protected void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

}

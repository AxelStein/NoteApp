package com.axel_stein.noteapp.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;
import com.axel_stein.noteapp.dialogs.drive.ConfirmExportDialog;
import com.axel_stein.noteapp.dialogs.drive.ConfirmImportDialog;
import com.axel_stein.noteapp.google_drive.DriveServiceHelper;
import com.axel_stein.noteapp.utils.DateFormatter;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.data.AppSettingsRepository.BACKUP_FILE_NAME;
import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class UserActivity extends BaseActivity implements ConfirmDialog.OnConfirmListener {

    @Inject
    CreateBackupInteractor mCreateBackupInteractor;

    @Inject
    ImportBackupInteractor mImportBackupInteractor;

    @Inject
    DriveServiceHelper mDriveServiceHelper;

    @Inject
    AppSettingsRepository mSettings;

    private TextView mTextLastSync;

    private ContentLoadingProgressBar mProgressBar;

    private SwitchCompat mSwitchAutoSync;

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

        mSwitchAutoSync = findViewById(R.id.switch_auto_sync);
        mSwitchAutoSync.setChecked(mSettings.autoSyncEnabled());
        mSwitchAutoSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enable) {
                mSettings.enableAutoSync(enable);
            }
        });
        mProgressBar = findViewById(R.id.progress_bar);
        TextView mTextUserName = findViewById(R.id.text_user_name);
        TextView mTextUserEmail = findViewById(R.id.text_user_email);
        CircleImageView mUserPhoto = findViewById(R.id.user_photo);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            ViewUtil.show(mTextUserName, mTextUserEmail);

            mTextUserName.setText(account.getDisplayName());
            mTextUserEmail.setText(account.getEmail());

            Picasso.get().load(account.getPhotoUrl()).placeholder(R.drawable.ic_account_circle_36dp).into(mUserPhoto);
        } else {
            ViewUtil.hide(mTextUserName, mTextUserEmail);
        }

        findViewById(R.id.text_import_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmImportDialog.launch(UserActivity.this);
            }
        });

        findViewById(R.id.text_export_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmExportDialog.launch(UserActivity.this);
            }
        });

        mTextLastSync = findViewById(R.id.text_last_sync);
        updateLastSyncTime();

        setupAds();
    }

    @Subscribe
    public void onRecreate(EventBusHelper.Recreate e) {
        finish();
    }

    @SuppressLint("CheckResult")
    private void exportDrive() {
        mProgressBar.show();

        mCreateBackupInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(String content) {
                        mDriveServiceHelper.uploadFile(BACKUP_FILE_NAME, content, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                updateLastSyncTime();
                                mProgressBar.hide();
                                EventBusHelper.showMessage(R.string.msg_export_success);
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                EventBusHelper.showMessage(R.string.error);
                                mProgressBar.hide();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mProgressBar.hide();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    private void importDrive() {
        mProgressBar.show();

        mDriveServiceHelper.downloadFile(BACKUP_FILE_NAME, new OnSuccessListener<String>() {
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
                                EventBusHelper.importCompleted();
                                EventBusHelper.recreate();
                                EventBusHelper.showMessage(R.string.msg_import_success);
                                mProgressBar.hide();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                mProgressBar.hide();
                                EventBusHelper.showMessage(R.string.error);
                            }
                        });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                mProgressBar.hide();
                EventBusHelper.showMessage(R.string.error);
            }
        });
    }

    private void updateLastSyncTime() {
        mProgressBar.show();

        mDriveServiceHelper.getFileModifiedDate(BACKUP_FILE_NAME, new OnSuccessListener<Long>() {
            @Override
            public void onSuccess(Long val) {
                String formattedDate = DateFormatter.formatDateTime(UserActivity.this, val);
                if (isEmpty(formattedDate)) {
                    formattedDate = "-";
                }
                ViewUtil.setText(mTextLastSync, getString(R.string.text_last_synced) + " " + formattedDate);

                mProgressBar.hide();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                mProgressBar.hide();
                ViewUtil.setText(mTextLastSync, getString(R.string.text_last_synced) + " -");
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
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    EventBusHelper.showMessage(R.string.error);
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

    @Override
    public void onConfirm(String tag) {
        switch (tag) {
            case ConfirmExportDialog.TAG:
                exportDrive();
                break;

            case ConfirmImportDialog.TAG:
                importDrive();
                break;
        }
    }

    @Override
    public void onCancel(String tag) {

    }

}

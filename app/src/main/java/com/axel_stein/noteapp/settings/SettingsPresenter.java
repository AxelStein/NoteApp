package com.axel_stein.noteapp.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.google_drive.GoogleDriveInteractor;
import com.axel_stein.noteapp.settings.SettingsContract.View;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.axel_stein.noteapp.utils.FileUtil.writeToFile;

public class SettingsPresenter implements SettingsContract.Presenter {

    @Inject
    CreateBackupInteractor mCreateBackupInteractor;

    @Inject
    ImportBackupInteractor mImportBackupInteractor;

    @Inject
    AppSettingsRepository mSettings;

    @Inject
    GoogleDriveInteractor mGoogleDrive;

    private View mView;
    private Context mContext;

    @Override
    public void onCreate(FragmentActivity activity) {
        App.getAppComponent().inject(this);
    }

    @Override
    public void onCreateView(View view) {
        mView = view;
        mContext = mView.getContext();

        String title = mContext.getString(R.string.app_version_title);
        String number = mContext.getString(R.string.app_version_number);
        mView.setAppVersion(String.format("%s %s", title, number));
    }

    @Override
    public void onDestroyView() {
        mView = null;
        mContext = null;
    }

    @Override
    public void onPreferenceClick(String key) {
        switch (key) {
            case "export_file":
                createBackup();
                break;

            case "import_file":
                if (mView != null) {
                    mView.startImportFileActivity();
                }
                break;

            case "rate_app":
                if (mView != null) {
                    mView.startRateAppActivity();
                }
                break;
        }
    }

    @SuppressLint("CheckResult")
    private void createBackup() {
        mCreateBackupInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String backup) {
                        String date = new SimpleDateFormat("dd-MM-yyyy-kkmm", Locale.ROOT).format(new Date());
                        String ext = "json";

                        String fileName = String.format("notes_%s.%s", date, ext);

                        File dir = mContext.getFilesDir();
                        File file = writeToFile(dir, fileName, backup);

                        if (mView != null) {
                            mView.startExportFileActivity(file);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        showMessage(R.string.error);
                    }
                });
    }

    @Override
    public void onFileImport(String backup) {
        mImportBackupInteractor.execute(backup)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        if (mView != null) {
                            mView.showImportDialog();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mView != null) {
                            mView.dismissImportDialog();
                            EventBusHelper.updateNoteList(false, true);
                            EventBusHelper.recreate();
                            // fixme
                            EventBusHelper.showMessage(R.string.msg_import_success, 1000);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        if (mView != null) {
                            mView.dismissImportDialog();
                            mView.showMessage(R.string.error);
                        }
                    }
                });
    }

    private void showMessage(int msg) {
        if (mView != null) {
            mView.showMessage(msg);
        }
    }

    @Override
    public void onPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case AppSettingsRepository.PREF_NIGHT_MODE:
                EventBusHelper.recreate();
                break;

            case AppSettingsRepository.PREF_SHOW_NOTES_CONTENT:
                EventBusHelper.updateNoteList();
                break;
        }
        mGoogleDrive.notifySettingsChanged(mSettings.exportSettings());
    }

}

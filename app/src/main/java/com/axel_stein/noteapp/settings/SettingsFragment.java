package com.axel_stein.noteapp.settings;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.LoadingDialog;
import com.axel_stein.noteapp.utils.FileUtil;

import java.io.File;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

@SuppressLint("CheckResult")
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, SettingsContract.View {

    private static final int REQUEST_CODE_PICK_FILE = 110;
    private SettingsPresenter mPresenter = new SettingsPresenter();

    private LoadingDialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        mPresenter.onCreate(getActivity());
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        mPresenter.onPreferenceClick(key);
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        mPresenter.onPreferenceChanged(sharedPreferences, s);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPresenter.onCreateView(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        mPresenter.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void startExportFileActivity(File file) {
        Uri fileUri = FileProvider.getUriForFile(getContext(), "com.axel_stein.noteapp.fileprovider", file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setFlags(FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_READ_URI_PERMISSION);

        // Workaround for Android bug.
        // grantUriPermission also needed for KITKAT,
        // see https://code.google.com/p/android/issues/detail?id=76683
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<ResolveInfo> resInfoList = getContext().getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, fileUri, FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("TAG", "Export: no activity found");
            showMessage(R.string.error_share);
        }
    }

    @Override
    public void startImportFileActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    @Override
    public void showImportDialog() {
        mDialog = LoadingDialog.from(R.string.title_import, R.string.msg_wait);
        mDialog.show(getFragmentManager());
    }

    @Override
    public void dismissImportDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void showMessage(int msg) {
        View v = getView();
        if (v != null) {
            Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startRateAppActivity() {
        final String packageName = getContext().getPackageName();

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException e) {
            e.printStackTrace();
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    @Override
    public void setAppVersion(String version) {
        Preference pref = findPreference("app_version");
        pref.setSummary(version);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_PICK_FILE:
                if (data == null) {
                    return;
                }

                ContentResolver cr = getContext().getContentResolver();
                Uri uri = data.getData();
                if (uri == null) {
                    Log.e("TAG", "data.getData() = null");
                    showMessage(R.string.error);
                    return;
                }

                try {
                    String src = FileUtil.convertStreamToString(cr.openInputStream(uri));
                    mPresenter.onFileImport(src);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(R.string.error);
                }

                break;
        }
    }

    /*
        findPreference("PREF_SHOW_ADD_NOTE_FAB").setVisible(false);
        findPreference("PREF_SHOW_ADD_NOTE_FAB").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                EventBusHelper.updateAddNoteFAB();
                return false;
            }
        });
        */

        /*
        findPreference("show_fab").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                EventBusHelper.recreate();
                return false;
            }
        });

        findPreference("enable_counters").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mSettingsRepository.enableCounters(!mSettingsRepository.countersEnabled());
                EventBusHelper.recreate();
                EventBusHelper.updateDrawer();
                return false;
            }
        });
        */

        /*
        findPreference("PREF_SHOW_NOTES_CONTENT").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                EventBusHelper.updateNoteList();
                return false;
            }
        });

        SwitchPreferenceCompat nightMode = (SwitchPreferenceCompat) findPreference("PREF_NIGHT_MODE");
        nightMode.setChecked(mSettingsRepository.nightMode());
        nightMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mSettingsRepository.setNightMode(!mSettingsRepository.nightMode());
                EventBusHelper.recreate();
                return true;
            }
        });
        */

        /*
        findPreference("secure_notes").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean reset = mSettingsRepository.showPasswordInput();

                PasswordDialog dialog = new PasswordDialog();
                dialog.setTitle(reset ? R.string.title_confirm_password : R.string.title_password_setup);
                dialog.setPositiveButtonText(R.string.action_ok);
                dialog.setShowMessage(!reset);
                dialog.setNegativeButtonText(R.string.action_cancel);
                dialog.setTargetFragment(SettingsFragment.this, 0);
                dialog.show(getFragmentManager(), null);
                return true;
            }
        });
        */

        /*
        findPreference("export_file").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mCreateBackupInteractor.execute()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String backup) throws Exception {
                                exportImpl(backup);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                showMessage(R.string.error);
                            }
                        });
                return true;
            }
        });

        findPreference("import_file").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*\*");
                startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
                return true;
            }
        });
        */

        /*
        findPreference("rate_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String packageName = getContext().getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    e.printStackTrace();
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
                }
                return true;
            }
        });

        Preference appVersion = findPreference("app_version");
        appVersion.setSummary(String.format("%s %s", getString(R.string.app_version_title), getString(R.string.app_version_number)));

        */
        /*
        findPreference("backup_manager").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(), BackupActivity.class));
                return true;
            }
        });
        */

    /*
    private void exportImpl(String backup) {
        //String fileName = "notes_" + new SimpleDateFormat("ddMMyyyykkmm", Locale.ROOT).format(new Date());
        String fileName = "notes_backup";
        File dir = getContext().getFilesDir();
        File file = writeToFile(dir, fileName, backup);

        Uri fileUri = FileProvider.getUriForFile(getContext(), "com.axel_stein.noteapp.fileprovider", file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setFlags(FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_READ_URI_PERMISSION);

        // Workaround for Android bug.
        // grantUriPermission also needed for KITKAT,
        // see https://code.google.com/p/android/issues/detail?id=76683
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<ResolveInfo> resInfoList = getContext().getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, fileUri, FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("TAG", "Export: no activity found");
            showMessage(R.string.error_share);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        try {
            ContentResolver cr = getContext().getContentResolver();
            Uri uri = data.getData();
            if (uri == null) {
                Log.e("TAG", "data.getData() = null");
                showMessage(R.string.error);
                return;
            }
            String src = FileUtil.convertStreamToString(cr.openInputStream(uri));

            mImportBackupInteractor.execute(src)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        private LoadingDialog mDialog;

                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            mDialog = LoadingDialog.from(R.string.title_import, R.string.msg_wait);
                            mDialog.show(getFragmentManager());
                        }

                        @Override
                        public void onComplete() {
                            dismissDialog();

                            showMessage(R.string.msg_import_success);
                            EventBusHelper.updateNoteList(false, true);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            dismissDialog();

                            e.printStackTrace();
                            showMessage(R.string.error);
                        }

                        private void dismissDialog() {
                            if (mDialog != null) {
                                mDialog.dismiss();
                                mDialog = null;
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(R.string.error);
        }
    }

    @Override
    public void onPasswordCommit(String password) {
        boolean reset = mSettingsRepository.showPasswordInput();
        if (reset) {
            if (mSettingsRepository.checkPassword(password)) {
                mSettingsRepository.setPassword(null);
                showMessage(R.string.msg_security_disabled);
            } else {
                showMessage(R.string.error_security_wrong_password);
            }
        } else {
            mSettingsRepository.setPassword(password);
            showMessage(R.string.msg_security_enabled);
        }
    }

    private void showMessage(int msgRes) {
        View v = getView();
        if (v != null) {
            Snackbar.make(v, msgRes, Snackbar.LENGTH_SHORT).show();
        }
    }
    */

}

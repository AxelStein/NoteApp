package com.axel_stein.noteapp.settings;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.LoadingDialog;
import com.axel_stein.noteapp.utils.FileUtil;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

import javax.inject.Inject;

@SuppressLint("CheckResult")
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, SettingsContract.View {
    private static final int REQUEST_CODE_PICK_FILE = 110;

    private final SettingsPresenter mPresenter = new SettingsPresenter();
    private LoadingDialog mDialog;

    @Inject
    public AppSettingsRepository mAppSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        PreferenceManager.getDefaultSharedPreferences(requireActivity()).registerOnSharedPreferenceChangeListener(this);
        mPresenter.onCreate(getActivity());
    }

    @Override
    public void onDestroy() {
        mPresenter.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(requireActivity()).unregisterOnSharedPreferenceChangeListener(this);
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
        App.getAppComponent().inject(this);

        setPreferencesFromResource(R.xml.settings, null);

        Preference pref = findPreference("purchases_category");
        if (pref != null) {
            // pref.setVisible(mAppSettings.adsEnabled());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        Context context = requireContext();
        Uri fileUri = FileProvider.getUriForFile(context, "com.axel_stein.noteapp.fileprovider", file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setFlags(FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_READ_URI_PERMISSION);

        // Workaround for Android bug.
        // grantUriPermission also needed for KITKAT,
        // see https://code.google.com/p/android/issues/detail?id=76683
        if (intent.resolveActivity(context.getPackageManager()) != null) {
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
    public void showMessage(String msg) {
        if (msg == null || msg.isEmpty()) return;
        View v = getView();
        if (v != null) {
            Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startRateAppActivity() {
        final String packageName = requireContext().getPackageName();

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
        if (pref != null) {
            pref.setSummary(version);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FILE) {
            if (data == null) {
                return;
            }

            ContentResolver cr = requireContext().getContentResolver();
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
        }
    }

}

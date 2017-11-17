package com.axel_stein.noteapp.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.label.AddLabelDialog;
import com.axel_stein.noteapp.dialogs.label.DeleteLabelDialog;
import com.axel_stein.noteapp.dialogs.label.RenameLabelDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.DeleteNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.RenameNotebookDialog;
import com.axel_stein.noteapp.notes.list.SearchActivity;
import com.axel_stein.noteapp.notes.list.TrashActivity;
import com.axel_stein.noteapp.settings.SettingsActivity;

import javax.inject.Inject;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements Screen {

    protected boolean mHandleHomeButton;
    @Inject
    AppSettingsRepository mAppSettings;

    protected boolean mNightMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getAppComponent().inject(this);

        mNightMode = mAppSettings.nightMode();
        setTheme(mNightMode ? R.style.AppThemeDark : R.style.AppTheme);
    }

    public void setHandleHomeButton(boolean handleHomeButton) {
        mHandleHomeButton = handleHomeButton;
    }

    protected void setFragment(@Nullable Fragment fragment, String tag) {
        if (fragment == null) {
            return;
        }

        try {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.content, fragment, tag)
                    .commit();
        } catch (Exception e) {
            // Catch IllegalStateException: Can not perform this action after onSaveInstanceState
            e.printStackTrace();
        }
    }

    @Override
    public void showTrashActivity() {
        startActivity(new Intent(this, TrashActivity.class));
    }

    @Override
    public void addNotebookDialog() {
        AddNotebookDialog.launch(this);
    }

    @Override
    public void renameNotebookDialog(@NonNull Notebook notebook) {
        RenameNotebookDialog.launch(this, notebook);
    }

    @Override
    public void deleteNotebookDialog(@NonNull Notebook notebook) {
        DeleteNotebookDialog.launch(this, notebook);
    }

    @Override
    public void addLabelDialog() {
        AddLabelDialog.launch(this);
    }

    @Override
    public void renameLabelDialog(@NonNull Label label) {
        RenameLabelDialog.launch(this, label);
    }

    @Override
    public void deleteLabelDialog(@NonNull Label label) {
        DeleteLabelDialog.launch(this, label);
    }

    @Override
    public void showSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void showSearchActivity() {
        startActivity(new Intent(this, SearchActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mHandleHomeButton && item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

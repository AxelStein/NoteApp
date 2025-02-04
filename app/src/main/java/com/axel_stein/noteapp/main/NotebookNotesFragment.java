package com.axel_stein.noteapp.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.notebook.GetNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.bottom_menu.BottomMenuDialog;
import com.axel_stein.noteapp.dialogs.notebook.DeleteNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.RenameNotebookDialog;
import com.axel_stein.noteapp.main.list.NotesFragment;
import com.axel_stein.noteapp.main.list.presenters.NotebookNotesPresenter;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.SimpleSingleObserver;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class NotebookNotesFragment extends NotesFragment implements BottomMenuDialog.OnMenuItemClickListener {
    private static final String TAG_SORT_NOTEBOOK_NOTES = "TAG_SORT_NOTEBOOK_NOTES";
    private static final String BUNDLE_NOTEBOOK_ID = "BUNDLE_NOTEBOOK_ID";

    private String mNotebookId;
    private Notebook mNotebook;
    private OnTitleChangeListener mOnTitleChangeListener;

    @Inject
    AppSettingsRepository mAppSettings;

    @Inject
    GetNotebookInteractor mNotebookInteractor;

    public void setNotebookId(String notebookId) {
        this.mNotebookId = notebookId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setHasOptionsMenu(true);
        if (mPresenter == null) {
            if (savedInstanceState != null) {
                mNotebookId = savedInstanceState.getString(BUNDLE_NOTEBOOK_ID);
            }
            setPresenter(new NotebookNotesPresenter(mNotebookId));
        }
        setEmptyMsg(getString(R.string.empty_notes));
        setPaddingTop(8);
        setPaddingBottom(88);
    }

    @Override
    public void onStart() {
        super.onStart();
        mNotebookInteractor.execute(mNotebookId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleSingleObserver<Notebook>() {
                    @Override
                    public void onSuccess(Notebook notebook) {
                        Activity activity = getActivity();
                        if (activity instanceof OnTitleChangeListener) {
                            mOnTitleChangeListener = (OnTitleChangeListener) activity;
                        }
                        updateNotebook(notebook);
                    }
                });
    }

    private void updateNotebook(Notebook notebook) {
        mNotebook = notebook;
        if (mOnTitleChangeListener != null) {
            mOnTitleChangeListener.onTitleChange(notebook.getTitle());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_NOTEBOOK_ID, mNotebookId);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mNotebookId = savedInstanceState.getString(BUNDLE_NOTEBOOK_ID);
        }
        if (mPresenter == null) {
            setPresenter(new NotebookNotesPresenter(mNotebookId));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_notebook, menu);
        MenuUtil.tintMenuIconsAttr(getContext(), menu, R.attr.menuItemTintColor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_options) {
            if (mPresenter != null) {
                mPresenter.showSortMenu();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showSortDialog(int itemId) {
        BottomMenuDialog.Builder builder = new BottomMenuDialog.Builder();
        builder.setTitle(getString(R.string.action_sort));
        builder.setMenuRes(R.menu.fragment_notebook_notes);
        builder.setCheckedItemId(itemId);
        builder.show(this, TAG_SORT_NOTEBOOK_NOTES);
    }

    @Override
    public void onMenuItemClick(BottomMenuDialog dialog, String tag, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_rename_notebook) {
            if (mNotebook != null) {
                RenameNotebookDialog.launch(this, mNotebook);
            }
        } else if (itemId == R.id.menu_delete_notebook) {
            if (mNotebook != null) {
                DeleteNotebookDialog.launch(getContext(), this, mNotebook);
            }
        } else {
            super.onMenuItemClick(dialog, tag, item);
        }
        dialog.dismiss();
    }

    @Subscribe
    public void onNotebookRenamed(EventBusHelper.RenameNotebook e) {
        updateNotebook(e.getNotebook());
    }

}

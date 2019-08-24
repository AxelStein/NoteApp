package com.axel_stein.noteapp.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class NotebookNotesFragment extends NotesFragment implements BottomMenuDialog.OnMenuItemClickListener {
    private static final String TAG_SORT_NOTEBOOK_NOTES = "TAG_SORT_NOTEBOOK_NOTES";

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
        EventBusHelper.subscribe(this);
        App.getAppComponent().inject(this);
        setHasOptionsMenu(true);

        if (mPresenter == null) {
            setPresenter(new NotebookNotesPresenter(mNotebookId));
        }

        setEmptyMsg(getString(R.string.empty_notebook));
        setPaddingTop(8);
        setPaddingBottom(88);
    }

    @Override
    public void onStart() {
        super.onStart();

        Activity activity = getActivity();
        if (activity instanceof OnTitleChangeListener) {
            mOnTitleChangeListener = (OnTitleChangeListener) activity;
        }

        mNotebookInteractor.execute(mNotebookId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Notebook>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Notebook notebook) {
                        updateNotebook(notebook);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    private void updateNotebook(Notebook notebook) {
        mNotebook = notebook;
        if (mOnTitleChangeListener != null) {
            mOnTitleChangeListener.onTitleChange(notebook.getTitle());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
        builder.addChecked(itemId);
        builder.show(this, TAG_SORT_NOTEBOOK_NOTES);
    }

    @Override
    public void onMenuItemClick(BottomMenuDialog dialog, String tag, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_rename_notebook:
                if (mNotebook != null) {
                    RenameNotebookDialog.launch(this, mNotebook);
                }
                break;

            case R.id.menu_delete_notebook:
                if (mNotebook != null) {
                    DeleteNotebookDialog.launch(getContext(), this, mNotebook);
                }
                break;

            default:
                super.onMenuItemClick(dialog, tag, item);

        }

        dialog.dismiss();
    }

    @Subscribe
    public void onNotebookRenamed(EventBusHelper.RenameNotebook e) {
        updateNotebook(e.getNotebook());
    }

}

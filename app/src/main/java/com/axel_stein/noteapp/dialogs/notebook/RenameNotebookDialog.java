package com.axel_stein.noteapp.dialogs.notebook;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.UpdateNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.EditTextDialog;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class RenameNotebookDialog extends EditTextDialog {

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;
    @Inject
    UpdateNotebookInteractor mUpdateNotebookInteractor;
    private Notebook mNotebook;
    private HashMap<String, Boolean> mMap;

    public static void launch(AppCompatActivity activity, Notebook notebook) {
        checkNotNull(activity);

        launch(activity.getSupportFragmentManager(), notebook);
    }

    public static void launch(Fragment fragment, Notebook notebook) {
        checkNotNull(fragment);

        RenameNotebookDialog dialog = createDialog(notebook);
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), null);
    }

    public static void launch(FragmentManager manager, Notebook notebook) {
        checkNotNull(manager);

        createDialog(notebook).show(manager, null);
    }

    private static RenameNotebookDialog createDialog(Notebook notebook) {
        checkNotNull(notebook);

        RenameNotebookDialog dialog = new RenameNotebookDialog();
        dialog.mNotebook = notebook;
        dialog.setHint(R.string.hint_notebook);
        dialog.setTitle(R.string.title_rename_notebook);
        dialog.setText(notebook.getTitle());
        dialog.setPositiveButtonText(R.string.action_rename);
        dialog.setNegativeButtonText(R.string.action_cancel);
        return dialog;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);

        if (mMap != null && mMap.size() > 0) {
            return;
        }

        mQueryNotebookInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Notebook>>() {
                    @Override
                    public void accept(List<Notebook> notebooks) throws Exception {
                        if (mMap == null) {
                            mMap = new HashMap<>();
                        } else {
                            mMap.clear();
                        }
                        for (Notebook notebook : notebooks) {
                            mMap.put(notebook.getTitle(), true);
                        }
                        setSuggestions(mMap);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                        EventBusHelper.showMessage(R.string.error);
                        dismiss();
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onTextCommit(final String text) {
        mNotebook.setTitle(text);
        mUpdateNotebookInteractor.execute(mNotebook)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        EventBusHelper.showMessage(R.string.msg_notebook_renamed);
                        EventBusHelper.renameNotebook(mNotebook);
                        dismiss();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                        EventBusHelper.showMessage(R.string.error);
                        dismiss();
                    }
                });
    }
}

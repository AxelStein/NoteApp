package com.axel_stein.noteapp.dialogs.notebook;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.domain.interactor.notebook.GetNotebookInteractor;
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

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class RenameNotebookDialog extends EditTextDialog {
    private static final String BUNDLE_NOTEBOOK_ID = "BUNDLE_NOTEBOOK_ID";

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Inject
    UpdateNotebookInteractor mUpdateNotebookInteractor;

    @Inject
    GetNotebookInteractor mGetNotebookInteractor;

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
        assert fragment.getFragmentManager() != null;
        dialog.show(fragment.getFragmentManager(), null);
    }

    private static void launch(FragmentManager manager, Notebook notebook) {
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_NOTEBOOK_ID, mNotebook.getId());
    }

    @SuppressLint("CheckResult")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);

        if (savedInstanceState != null) {
            String id = savedInstanceState.getString(BUNDLE_NOTEBOOK_ID);
            mGetNotebookInteractor.execute(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Notebook>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Notebook notebook) {
                            mNotebook = notebook;
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
        } else if (mMap == null || mMap.size() == 0){
            mQueryNotebookInteractor.execute()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<List<Notebook>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(List<Notebook> notebooks) {
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

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();

                            EventBusHelper.showMessage(R.string.error);
                            dismiss();
                        }
                    });
        }
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onTextCommit(final String text) {
        if (mNotebook == null) {
            EventBusHelper.showMessage(R.string.error);
            return;
        }

        mNotebook.setTitle(text);
        mUpdateNotebookInteractor.execute(mNotebook)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        EventBusHelper.showMessage(R.string.msg_notebook_renamed);
                        EventBusHelper.renameNotebook(mNotebook);
                        dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                        EventBusHelper.showMessage(R.string.error);
                        dismiss();
                    }
                });
    }
}

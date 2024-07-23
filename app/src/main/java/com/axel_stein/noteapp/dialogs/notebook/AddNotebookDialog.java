package com.axel_stein.noteapp.dialogs.notebook;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.domain.interactor.notebook.InsertNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
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

public class AddNotebookDialog extends EditTextDialog {

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Inject
    InsertNotebookInteractor mInsertNotebookInteractor;

    private HashMap<String, Boolean> mMap;

    public static void launch(FragmentActivity activity) {
        launch(activity.getSupportFragmentManager());
    }

    public static void launch(Fragment fragment) {
        checkNotNull(fragment);

        AddNotebookDialog dialog = createDialog();
        dialog.setTargetFragment(fragment, 0);
        assert fragment.getFragmentManager() != null;
        dialog.show(fragment.getFragmentManager(), null);
    }

    private static void launch(FragmentManager manager) {
        createDialog().show(manager, null);
    }

    private static AddNotebookDialog createDialog() {
        AddNotebookDialog dialog = new AddNotebookDialog();
        dialog.setHint(R.string.hint_notebook);
        dialog.setTitle(R.string.title_add_notebook);
        dialog.setPositiveButtonText(R.string.action_add);
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
                    EventBusHelper.showMessage(R.string.error);
                    e.printStackTrace();
                    dismiss();
                }
            });
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onTextCommit(final String text) {
        final Notebook notebook = new Notebook();
        notebook.setTitle(text);

        mInsertNotebookInteractor.execute(notebook)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new CompletableObserver() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onComplete() {
                    EventBusHelper.showMessage(R.string.msg_notebook_added);
                    EventBusHelper.addNotebook(notebook);
                    dismiss();
                }

                @Override
                public void onError(Throwable e) {
                    EventBusHelper.showMessage(R.string.error);
                    e.printStackTrace();
                    dismiss();
                }
            });
    }
}


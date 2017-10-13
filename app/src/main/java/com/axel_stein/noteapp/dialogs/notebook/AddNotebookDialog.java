package com.axel_stein.noteapp.dialogs.notebook;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static android.support.v4.util.Preconditions.checkNotNull;

public class AddNotebookDialog extends EditTextDialog {

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;
    @Inject
    InsertNotebookInteractor mInsertNotebookInteractor;
    private HashMap<String, Boolean> mMap;

    public static void launch(AppCompatActivity activity) {
        checkNotNull(activity);

        launch(activity.getSupportFragmentManager());
    }

    public static void launch(Fragment fragment) {
        checkNotNull(fragment);

        AddNotebookDialog dialog = createDialog();
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), null);
    }

    public static void launch(FragmentManager manager) {
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

    @Override
    protected void onTextCommit(final String text) {
        final Notebook notebook = new Notebook();
        notebook.setTitle(text);

        mInsertNotebookInteractor.execute(notebook)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        EventBusHelper.showMessage(R.string.msg_notebook_added);
                        EventBusHelper.addNotebook(notebook);
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


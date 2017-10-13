package com.axel_stein.noteapp.dialogs.notebook;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.axel_stein.domain.interactor.notebook.DeleteNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static android.support.v4.util.Preconditions.checkNotNull;

public class DeleteNotebookDialog extends ConfirmDialog {

    @Inject
    DeleteNotebookInteractor mDeleteNotebookInteractor;
    private Notebook mNotebook;

    public static void launch(AppCompatActivity activity, Notebook notebook) {
        checkNotNull(activity);
        launch(activity, activity.getSupportFragmentManager(), notebook);
    }

    public static void launch(Context context, Fragment fragment, Notebook notebook) {
        checkNotNull(fragment);

        DeleteNotebookDialog dialog = createDialog(context, notebook);
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), null);
    }

    public static void launch(Context context, FragmentManager manager, Notebook notebook) {
        checkNotNull(manager);

        createDialog(context, notebook).show(manager, null);
    }

    private static DeleteNotebookDialog createDialog(Context context, Notebook notebook) {
        checkNotNull(context);
        checkNotNull(notebook);

        DeleteNotebookDialog dialog = new DeleteNotebookDialog();
        dialog.mNotebook = notebook;
        dialog.setTitle(context.getString(R.string.title_delete_notebook, notebook.getTitle()));
        dialog.setMessage(R.string.msg_delete_notebook);
        dialog.setPositiveButtonText(R.string.action_delete);
        dialog.setNegativeButtonText(R.string.action_cancel);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
    }

    @Override
    protected void onConfirm() {
        mDeleteNotebookInteractor.execute(mNotebook)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        EventBusHelper.showMessage(R.string.msg_note_deleted);
                        EventBusHelper.deleteNotebook(mNotebook);
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

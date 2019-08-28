package com.axel_stein.noteapp.dialogs.notebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class DeleteNotebookDialog extends ConfirmDialog {
    @Inject
    DeleteNotebookInteractor mDeleteNotebookInteractor;
    private Notebook mNotebook;

    public static void launch(Context context, Fragment fragment, Notebook notebook) {
        checkNotNull(fragment);

        DeleteNotebookDialog dialog = createDialog(context, notebook);
        dialog.setTargetFragment(fragment, 0);
        if (fragment.getFragmentManager() != null) {
            dialog.show(fragment.getFragmentManager(), null);
        }
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
        deleteImpl(mNotebook);
        dismiss();
    }

    @SuppressLint("CheckResult")
    private void deleteImpl(final Notebook notebook) {
        mDeleteNotebookInteractor.execute(notebook)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() {
                        EventBusHelper.showMessage(R.string.msg_notebook_deleted);
                        EventBusHelper.deleteNotebook(notebook);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

}

package com.axel_stein.noteapp.dialogs.notebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.axel_stein.domain.interactor.notebook.DeleteNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.GetNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class DeleteNotebookDialog extends ConfirmDialog {
    private static final String BUNDLE_NOTEBOOK_ID = "BUNDLE_NOTEBOOK_ID";

    @Inject
    DeleteNotebookInteractor mDeleteNotebookInteractor;

    @Inject
    GetNotebookInteractor mGetNotebookInteractor;

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
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_NOTEBOOK_ID, mNotebook.getId());
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
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        EventBusHelper.showMessage(R.string.msg_notebook_deleted);
                        EventBusHelper.deleteNotebook(notebook);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

}

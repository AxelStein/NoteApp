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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class DeleteNotebookDialog extends ConfirmDialog {

    @Inject
    DeleteNotebookInteractor mDeleteNotebookInteractor;
    private List<Notebook> mList;
    private Queue<Notebook> mQueue;

    public static void launch(AppCompatActivity activity, Notebook notebook) {
        checkNotNull(activity);
        launch(activity, activity.getSupportFragmentManager(), notebook);
    }

    public static void launch(AppCompatActivity activity, List<Notebook> notebooks) {
        checkNotNull(activity);
        launch(activity, activity.getSupportFragmentManager(), notebooks);
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

    public static void launch(Context context, FragmentManager manager, List<Notebook> notebooks) {
        checkNotNull(manager);
        createDialog(context, notebooks).show(manager, null);
    }

    private static DeleteNotebookDialog createDialog(Context context, Notebook notebook) {
        checkNotNull(context);
        checkNotNull(notebook);

        List<Notebook> list = new ArrayList<>();
        list.add(notebook);
        return createDialog(context, list);
    }

    private static DeleteNotebookDialog createDialog(Context context, List<Notebook> notebooks) {
        checkNotNull(context);
        checkNotNull(notebooks);

        DeleteNotebookDialog dialog = new DeleteNotebookDialog();
        dialog.mList = notebooks;

        if (notebooks.size() == 1) {
            Notebook notebook = notebooks.get(0);
            dialog.setTitle(context.getString(R.string.title_delete_notebook, notebook.getTitle()));
        } else {
            dialog.setTitle(R.string.title_delete_notebooks);
        }
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
        if (mList != null) {
            mQueue = new LinkedBlockingQueue<>(mList);
            deleteImpl(mQueue.poll());
        }
        dismiss();
    }

    private void deleteImpl(final Notebook notebook) {
        if (notebook == null) {
            return;
        }
        mDeleteNotebookInteractor.execute(notebook)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Notebook next = mQueue.poll();
                        if (next == null) {
                            EventBusHelper.showMessage(R.string.msg_notebook_deleted);
                            EventBusHelper.deleteNotebook(notebook);
                        } else {
                            deleteImpl(next);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                        deleteImpl(mQueue.poll());
                    }
                });
    }

}

package com.axel_stein.noteapp.dialogs.label;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.domain.interactor.label.DeleteLabelInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class DeleteLabelDialog extends ConfirmDialog {

    @Inject
    DeleteLabelInteractor mDeleteLabelInteractor;
    private Label mLabel;

    public static void launch(@NonNull AppCompatActivity activity, @NonNull Label label) {
        checkNotNull(activity);

        launch(activity, activity.getSupportFragmentManager(), label);
    }

    public static void launch(@NonNull Context context, @NonNull Fragment fragment, @NonNull Label label) {
        checkNotNull(fragment);

        DeleteLabelDialog dialog = createDialog(context, label);
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), null);
    }

    public static void launch(@NonNull Context context, @NonNull FragmentManager manager, @NonNull Label label) {
        checkNotNull(manager);

        createDialog(context, label).show(manager, null);
    }

    private static DeleteLabelDialog createDialog(@NonNull Context context, @NonNull Label label) {
        checkNotNull(context);
        checkNotNull(label);

        DeleteLabelDialog dialog = new DeleteLabelDialog();
        dialog.mLabel = label;
        dialog.setTitle(context.getString(R.string.title_delete_label, label.getTitle()));
        dialog.setMessage(R.string.msg_delete_label);
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
        mDeleteLabelInteractor.execute(mLabel)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        EventBusHelper.showMessage(R.string.msg_label_deleted);
                        EventBusHelper.deleteLabel(mLabel);
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

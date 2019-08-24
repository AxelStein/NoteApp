package com.axel_stein.noteapp.dialogs.label;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateLabelInteractor;
import com.axel_stein.domain.model.Label;
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

public class RenameLabelDialog extends EditTextDialog {

    @Inject
    QueryLabelInteractor mQueryLabelInteractor;
    @Inject
    UpdateLabelInteractor mUpdateLabelInteractor;

    private Label mLabel;
    private HashMap<String, Boolean> mMap;

    public static void launch(AppCompatActivity activity, Label label) {
        checkNotNull(activity);

        launch(activity.getSupportFragmentManager(), label);
    }

    public static void launch(FragmentManager manager, Label label) {
        checkNotNull(manager);

        createDialog(label).show(manager, null);
    }

    private static RenameLabelDialog createDialog(Label label) {
        checkNotNull(label);

        RenameLabelDialog dialog = new RenameLabelDialog();
        dialog.mLabel = label;
        dialog.setHint(R.string.hint_label);
        dialog.setTitle(R.string.title_rename_label);
        dialog.setText(label.getTitle());
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

        mQueryLabelInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Label>>() {
                    @Override
                    public void accept(List<Label> labels) {
                        if (mMap == null) {
                            mMap = new HashMap<>();
                        } else {
                            mMap.clear();
                        }
                        for (Label label : labels) {
                            mMap.put(label.getTitle(), true);
                        }
                        setSuggestions(mMap);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();

                        EventBusHelper.showMessage(R.string.error);
                        dismiss();
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onTextCommit(final String text) {
        mLabel.setTitle(text);
        mUpdateLabelInteractor.execute(mLabel)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() {
                        EventBusHelper.showMessage(R.string.msg_label_renamed);
                        EventBusHelper.renameLabel(mLabel);
                        dismiss();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();

                        EventBusHelper.showMessage(R.string.error);
                        dismiss();
                    }
                });
    }
}


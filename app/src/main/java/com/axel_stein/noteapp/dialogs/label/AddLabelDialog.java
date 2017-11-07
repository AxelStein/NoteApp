package com.axel_stein.noteapp.dialogs.label;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.axel_stein.domain.interactor.label.InsertLabelInteractor;
import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
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

public class AddLabelDialog extends EditTextDialog {

    @Inject
    QueryLabelInteractor mQueryLabelInteractor;
    @Inject
    InsertLabelInteractor mInsertLabelInteractor;
    private HashMap<String, Boolean> mMap;

    public static void launch(AppCompatActivity activity) {
        checkNotNull(activity);

        launch(activity.getSupportFragmentManager());
    }

    public static void launch(Fragment fragment) {
        checkNotNull(fragment);

        AddLabelDialog dialog = createDialog();
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), null);
    }

    public static void launch(FragmentManager manager) {
        checkNotNull(manager);

        createDialog().show(manager, null);
    }

    private static AddLabelDialog createDialog() {
        AddLabelDialog dialog = new AddLabelDialog();
        dialog.setHint(R.string.hint_label);
        dialog.setTitle(R.string.title_add_label);
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

        mQueryLabelInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Label>>() {
                    @Override
                    public void accept(List<Label> labels) throws Exception {
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
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                        EventBusHelper.showMessage(R.string.error);
                        dismiss();
                    }
                });
    }

    @Override
    protected void onTextCommit(final String text) {
        final Label label = new Label();
        label.setTitle(text);

        mInsertLabelInteractor.execute(label)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        EventBusHelper.showMessage(R.string.msg_label_added);
                        EventBusHelper.addLabel(label);
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


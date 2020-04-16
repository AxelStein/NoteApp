package com.axel_stein.noteapp.main;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.main.list.NotesFragment;
import com.axel_stein.noteapp.main.list.presenters.RemindersNotesPresenter;

import javax.inject.Inject;

public class RemindersFragment extends NotesFragment {
    @Inject
    AppSettingsRepository mAppSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setHasOptionsMenu(true);
        if (mPresenter == null) {
            setPresenter(new RemindersNotesPresenter());
        }
        setEmptyMsg(getString(R.string.empty_notes));
        setPaddingTop(8);
        setPaddingBottom(88);
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        if (activity instanceof OnTitleChangeListener) {
            OnTitleChangeListener mListener = (OnTitleChangeListener) activity;
            mListener.onTitleChange(getString(R.string.action_reminders));
        }
    }

}

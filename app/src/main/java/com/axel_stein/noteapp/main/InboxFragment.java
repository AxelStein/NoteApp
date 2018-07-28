package com.axel_stein.noteapp.main;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notes.list.NotesFragment;
import com.axel_stein.noteapp.notes.list.presenters.InboxNotesPresenter;

import javax.inject.Inject;

public class InboxFragment extends NotesFragment {

    @Inject
    AppSettingsRepository mAppSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setHasOptionsMenu(true);
        setPresenter(new InboxNotesPresenter());
        setEmptyMsg(getString(R.string.empty_inbox));
        showBottomPadding(true);
    }

}

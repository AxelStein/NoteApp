package com.axel_stein.noteapp.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.main.list.NotesFragment;
import com.axel_stein.noteapp.main.list.presenters.InboxNotesPresenter;
import com.axel_stein.noteapp.utils.MenuUtil;

import javax.inject.Inject;

public class InboxFragment extends NotesFragment {

    @Inject
    AppSettingsRepository mAppSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setHasOptionsMenu(true);
        if (mPresenter == null) {
            setPresenter(new InboxNotesPresenter());
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
            mListener.onTitleChange(getString(R.string.action_inbox));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_inbox, menu);
        MenuUtil.tintMenuIconsAttr(getContext(), menu, R.attr.menuItemTintColor);
        //MenuUtil.tintMenuIconsColorRes(getContext(), menu, R.color.text_color_primary_light);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sort) {
            if (mPresenter != null) {
                mPresenter.showSortMenu();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

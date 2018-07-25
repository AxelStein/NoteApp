package com.axel_stein.noteapp.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notes.list.NotesFragment;
import com.axel_stein.noteapp.notes.list.presenters.HomeNotesPresenter;
import com.axel_stein.noteapp.utils.MenuUtil;

import java.util.HashMap;

import javax.inject.Inject;

public class HomeFragment extends NotesFragment {

    @Inject
    AppSettingsRepository mAppSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setHasOptionsMenu(true);
        setPresenter(new HomeNotesPresenter());
        setEmptyMsg(getString(R.string.empty_home));
        showBottomPadding(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_home, menu);
        MenuUtil.tintMenuIconsAttr(getContext(), menu, R.attr.menuItemTintColor);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        NoteOrder currentOrder = mAppSettings.getNotesOrder();
        if (currentOrder != null) {
            MenuItem item = menu.findItem(R.id.menu_sort);
            if (item != null) {
                MenuUtil.check(item.getSubMenu(), menuItemFromNoteOrder(currentOrder), true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NoteOrder order = noteOrderFromMenuItem(item);
        if (order != null) {
            item.setChecked(true);
            mAppSettings.setNotesOrder(order);
            EventBusHelper.updateNoteList();
            scrollToTop();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private NoteOrder noteOrderFromMenuItem(MenuItem item) {
        if (item == null) {
            return null;
        }

        SparseArray<NoteOrder> sparseArray = new SparseArray<>();
        sparseArray.put(R.id.menu_sort_title, NoteOrder.TITLE);
        sparseArray.put(R.id.menu_sort_relevance, NoteOrder.RELEVANCE);
        sparseArray.put(R.id.menu_created_newest, NoteOrder.CREATED_NEWEST);
        sparseArray.put(R.id.menu_created_oldest, NoteOrder.CREATED_OLDEST);
        sparseArray.put(R.id.menu_updated_newest, NoteOrder.MODIFIED_NEWEST);
        sparseArray.put(R.id.menu_updated_oldest, NoteOrder.MODIFIED_OLDEST);

        return sparseArray.get(item.getItemId());
    }

    private int menuItemFromNoteOrder(NoteOrder order) {
        if (order == null) {
            return -1;
        }

        HashMap<NoteOrder, Integer> map = new HashMap<>();
        map.put(NoteOrder.TITLE, R.id.menu_sort_title);
        map.put(NoteOrder.RELEVANCE, R.id.menu_sort_relevance);
        map.put(NoteOrder.CREATED_NEWEST, R.id.menu_created_newest);
        map.put(NoteOrder.CREATED_OLDEST, R.id.menu_created_oldest);
        map.put(NoteOrder.MODIFIED_NEWEST, R.id.menu_updated_newest);
        map.put(NoteOrder.MODIFIED_OLDEST, R.id.menu_updated_oldest);

        return map.get(order);
    }

}

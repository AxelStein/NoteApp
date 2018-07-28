package com.axel_stein.noteapp.notebook_manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.notebook.UpdateOrderNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.model.NotebookCache;
import com.axel_stein.domain.model.NotebookOrder;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.bottom_menu.BottomMenuDialog;
import com.axel_stein.noteapp.dialogs.notebook.DeleteNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.RenameNotebookDialog;
import com.axel_stein.noteapp.main.NoteListActivity;
import com.axel_stein.noteapp.main.SortPanelListener;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.IconTextView;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;
import static com.axel_stein.noteapp.utils.ViewUtil.setText;

public class NotebookManagerFragment extends Fragment implements NotebookManagerContract.View, BottomMenuDialog.OnMenuItemClickListener {
    private static final String TAG_ITEM_NOTEBOOK = "com.axel_stein.noteapp.notebook_manager.TAG_ITEM_NOTEBOOK";
    private static final String TAG_SORT_NOTEBOOKS = "com.axel_stein.noteapp.notebook_manager.TAG_SORT_NOTEBOOKS";
    private static final int SMART_NOTEBOOK_COUNT = 2;

    private ItemListener mListener = new ItemListener() {
        @Override
        public void onItemClick(int pos, Notebook notebook) {
            mPresenter.onItemClick(pos, notebook);
        }

        @Override
        public void onMenuItemClick(int pos, Notebook notebook) {
            BottomMenuDialog.Builder builder = new BottomMenuDialog.Builder();
            builder.setTitle(notebook.getTitle());
            builder.setMenuRes(R.menu.item_notebook);
            builder.setData(notebook);
            builder.show(NotebookManagerFragment.this, TAG_ITEM_NOTEBOOK);
        }
    };

    private Adapter mAdapter;

    private NotebookManagerPresenter mPresenter = new NotebookManagerPresenter();

    private View mEmptyView;

    private View mSortPanel;

    private TextView mTextCounter;

    private IconTextView mSortTitle;

    private boolean mNotEmptyList;

    @Inject
    AppSettingsRepository mAppSettings;

    @Inject
    UpdateOrderNotebookInteractor mOrderInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        EventBusHelper.subscribe(this);
    }

    @Override
    public void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notebook_manager, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.empty_view);

        mAdapter = new Adapter(mOrderInteractor);
        mAdapter.setItemListener(mListener);
        mAdapter.attachRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mPresenter.onCreateView(this);

        FragmentActivity activity = getActivity();
        if (activity != null && activity instanceof SortPanelListener) {
            SortPanelListener l = (SortPanelListener) activity;
            mSortPanel = l.getSortPanel();
            mTextCounter = l.getCounter();
            mSortTitle = l.getSortTitle();
            mSortTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAppSettings.toggleNotebookDescOrder();
                    NotebookCache.invalidate();
                    EventBusHelper.updateDrawer();

                }
            });
            mSortTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    launchSortDialog();
                    return true;
                }
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        mEmptyView = null;
        mSortPanel = null;
        mTextCounter = null;
        mSortTitle = null;
        mPresenter.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_notebook_manager, menu);
        MenuUtil.tintMenuIconsAttr(getContext(), menu, R.attr.menuItemTintColor);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuUtil.show(menu, mNotEmptyList, R.id.menu_sort);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort:
                launchSortDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchSortDialog() {
        BottomMenuDialog.Builder builder = new BottomMenuDialog.Builder();
        builder.setMenuRes(R.menu.sort_notebooks);
        builder.setChecked(menuItemFromOrder(mAppSettings.getNotebookOrder()));
        builder.show(this, TAG_SORT_NOTEBOOKS);
    }

    @Override
    public void onMenuItemClick(BottomMenuDialog dialog, String tag, MenuItem item) {
        switch (tag) {
            case TAG_ITEM_NOTEBOOK:
                handleNotebookMenuItemClick(item, (Notebook) dialog.getData());
                break;

            case TAG_SORT_NOTEBOOKS:
                handleSortMenuItemClick(item);
                break;
        }

        dialog.dismiss();
    }

    private void handleNotebookMenuItemClick(MenuItem item, Notebook notebook) {
        switch (item.getItemId()) {
            case R.id.menu_rename:
                RenameNotebookDialog.launch(getFragmentManager(), notebook);
                break;

            case R.id.menu_delete:
                DeleteNotebookDialog.launch(getContext(), getFragmentManager(), notebook);
                break;

            case R.id.menu_change_color: // todo
                break;
        }
    }

    private void handleSortMenuItemClick(MenuItem item) {
        NotebookOrder order = orderFromMenuItem(item);
        mAppSettings.setNotebookOrder(order);

        NotebookCache.invalidate();
        EventBusHelper.updateDrawer();
    }

    private int menuItemFromOrder(NotebookOrder order) {
        if (order == null) {
            return -1;
        }
        HashMap<NotebookOrder, Integer> map = new HashMap<>();
        map.put(NotebookOrder.TITLE, R.id.menu_sort_title);
        map.put(NotebookOrder.NOTE_COUNT, R.id.menu_sort_note_count);
        map.put(NotebookOrder.CUSTOM, R.id.menu_sort_custom);
        return map.get(order);
    }

    private NotebookOrder orderFromMenuItem(MenuItem item) {
        if (item == null) {
            return null;
        }
        SparseArray<NotebookOrder> sparseArray = new SparseArray<>();
        sparseArray.put(R.id.menu_sort_title, NotebookOrder.TITLE);
        sparseArray.put(R.id.menu_sort_note_count, NotebookOrder.NOTE_COUNT);
        sparseArray.put(R.id.menu_sort_custom, NotebookOrder.CUSTOM);
        return sparseArray.get(item.getItemId());
    }

    private void setSortText(NotebookOrder order) {
        int textRes = 0;
        boolean enable = true;
        switch (order) {
            case TITLE:
                textRes = R.string.action_sort_title;
                break;

            case NOTE_COUNT:
                textRes = R.string.action_sort_note_count;
                break;

            case CUSTOM:
                textRes = R.string.action_sort_custom;
                enable = false;
                break;
        }

        ViewUtil.setText(mSortTitle, getString(textRes));
        setSortIndicator(order.isDesc(), enable);
    }

    private void setSortIndicator(boolean desc, boolean enable) {
        if (mSortTitle != null) {
            if (enable) {
                mSortTitle.setIconRight(desc ? R.drawable.ic_arrow_downward_white_18dp : R.drawable.ic_arrow_upward_white_18dp);
            } else {
                mSortTitle.setIconRight(null);
            }
        }
    }

    private void showSortPanel(boolean show) {
        ViewUtil.show(show, mSortPanel);
    }

    private void setSortPanelCounterText(int notebookCount) {
        ViewUtil.setText(mTextCounter, getString(R.string.template_notebook_counter, notebookCount-SMART_NOTEBOOK_COUNT));
    }

    @Override
    public void setItems(List<Notebook> items) {
        if (mAdapter != null) {
            mAdapter.setItems(items);
        }

        ViewUtil.show(items != null && items.size() == 0, mEmptyView);

        mNotEmptyList = items != null && items.size() > SMART_NOTEBOOK_COUNT;
        showSortPanel(mNotEmptyList);
        if (items != null) {
            setSortPanelCounterText(items.size());
        }
        setSortText(mAppSettings.getNotebookOrder());

        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void confirmDeleteDialog(Notebook notebook) {
        DeleteNotebookDialog.launch((AppCompatActivity) getActivity(), notebook);
    }

    @Override
    public void startNoteListActivity(Notebook notebook) {
        NoteListActivity.launch(getContext(), notebook);
    }

    @Subscribe
    public void onRecreate(EventBusHelper.Recreate e) {
        mPresenter.forceUpdate();
    }

    @Subscribe
    public void updateDrawer(EventBusHelper.UpdateDrawer e) {
        mPresenter.forceUpdate();
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Subscribe
    public void addNotebook(EventBusHelper.AddNotebook e) {
        mPresenter.forceUpdate();
    }

    @Subscribe
    public void renameNotebook(EventBusHelper.RenameNotebook e) {
        mPresenter.forceUpdate();
    }

    @Subscribe
    public void deleteNotebook(EventBusHelper.DeleteNotebook e) {
        mPresenter.forceUpdate();
    }

    private interface ItemListener {

        void onItemClick(int pos, Notebook notebook);

        void onMenuItemClick(int pos, Notebook notebook);

    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private List<Notebook> mItems;

        private ItemListener mItemListener;

        private ItemTouchHelper mItemTouchHelper;

        private UpdateOrderNotebookInteractor mOrderInteractor;

        Adapter(UpdateOrderNotebookInteractor orderInteractor) {
            mOrderInteractor = orderInteractor;
            mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(UP | DOWN, 0) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    int pos1 = viewHolder.getAdapterPosition();
                    int pos2 = target.getAdapterPosition();

                    Notebook n1 = getItem(pos1);
                    Notebook n2 = getItem(pos2);

                    if (n1 == null && n2 == null) {
                        return false;
                    }
                    if (n1 != null && !n1.isEditable()) {
                        return false;
                    }
                    if (n2 != null && !n2.isEditable()) {
                        return false;
                    }

                    swapItems(pos1, pos2);

                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                }

                @Override
                public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder target) {
                    int pos = target.getAdapterPosition();
                    Notebook notebook = getItem(pos);
                    return notebook != null && notebook.isEditable() ? UP | DOWN : 0;
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    return true;
                }

                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    NotebookCache.invalidate();

                    List<Notebook> items = new ArrayList<>(mItems);
                    items.remove(0); // remove all
                    items.remove(0); // remove starred

                    mOrderInteractor.execute(items).subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            EventBusHelper.updateDrawer();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        }

        void attachRecyclerView(RecyclerView view) {
            mItemTouchHelper.attachToRecyclerView(view);
        }

        private void swapItems(int from, int to) {
            if (mItems != null && from >= 0 && to >= 0) {
                Collections.swap(mItems, from, to);
                notifyItemMoved(from, to);
            }
        }

        void setItems(List<Notebook> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        void setItemListener(ItemListener l) {
            mItemListener = l;
        }

        Notebook getItem(int position) {
            if (position >= 0 && position < getItemCount()) {
                return mItems.get(position);
            }
            return null;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_notebook_manager, parent, false);
            return new ViewHolder(v, mItemListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Notebook notebook = getItem(position);

            int iconRes = notebook.getIconRes();
            holder.mIcon.setImageResource(iconRes != 0 ? iconRes : R.drawable.ic_book_white_24dp);

            setText(holder.mTextTitle, notebook.getTitle());

            long count = notebook.getNoteCount();
            setText(holder.mTextBadge, count <= 0 ? null : String.valueOf(count));

            ViewUtil.show(notebook.isEditable(), holder.mMenu);
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView mIcon;

            TextView mTextTitle;

            TextView mTextBadge;

            ImageButton mMenu;

            ViewHolder(View itemView, final ItemListener l) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (l != null) {
                            int pos = getAdapterPosition();
                            if (pos >= 0 && pos < getItemCount()) {
                                l.onItemClick(pos, getItem(pos));
                            }
                        }
                    }
                });
                mIcon = itemView.findViewById(R.id.icon);
                mTextTitle = itemView.findViewById(R.id.text_title);
                mTextBadge = itemView.findViewById(R.id.text_badge);
                mMenu = itemView.findViewById(R.id.button_menu);
                mMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (l != null) {
                            int pos = getAdapterPosition();
                            if (pos >= 0 && pos < getItemCount()) {
                                l.onMenuItemClick(pos, getItem(pos));
                            }
                        }
                    }
                });
            }
        }

    }

}

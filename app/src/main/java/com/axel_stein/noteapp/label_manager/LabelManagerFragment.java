package com.axel_stein.noteapp.label_manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.label.UpdateOrderLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateViewsLabelInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.LabelCache;
import com.axel_stein.domain.model.LabelOrder;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.ScrollableFragment;
import com.axel_stein.noteapp.dialogs.bottom_menu.BottomMenuDialog;
import com.axel_stein.noteapp.dialogs.label.DeleteLabelDialog;
import com.axel_stein.noteapp.dialogs.label.RenameLabelDialog;
import com.axel_stein.noteapp.main.NoteListActivity;
import com.axel_stein.noteapp.main.SortPanelListener;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.IconTextView;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;
import static com.axel_stein.noteapp.utils.ViewUtil.setText;

public class LabelManagerFragment extends Fragment implements LabelManagerContract.View,
        BottomMenuDialog.OnMenuItemClickListener,
        ScrollableFragment {

    private static final String TAG_ITEM_LABEL = "com.axel_stein.noteapp.label_manager.TAG_ITEM_LABEL";
    private static final String TAG_SORT_LABELS = "com.axel_stein.noteapp.label_manager.TAG_SORT_LABELS";

    private ItemListener mListener = new ItemListener() {
        @Override
        public void onItemClick(int pos, Label label) {
            mPresenter.onItemClick(pos, label);
        }

        @Override
        public void onMenuItemClick(int pos, Label label) {
            BottomMenuDialog.Builder builder = new BottomMenuDialog.Builder();
            builder.setTitle(label.getTitle());
            builder.setMenuRes(R.menu.item_label);
            builder.setData(label);
            builder.show(LabelManagerFragment.this, TAG_ITEM_LABEL);
        }
    };

    private Adapter mAdapter;

    private LabelManagerPresenter mPresenter = new LabelManagerPresenter();

    private View mEmptyView;

    private View mSortPanel;

    private TextView mTextCounter;

    private IconTextView mSortTitle;

    private boolean mNotEmptyList;

    private RecyclerView mRecyclerView;

    @Inject
    AppSettingsRepository mAppSettings;

    @Inject
    UpdateOrderLabelInteractor mOrderInteractor;

    @Inject
    UpdateViewsLabelInteractor mViewsLabelInteractor;

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
        View view = inflater.inflate(R.layout.fragment_label_manager, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.empty_view);

        mAdapter = new Adapter(mOrderInteractor);
        mAdapter.setItemListener(mListener);
        mAdapter.attachRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FragmentActivity activity = getActivity();
        if (activity != null && activity instanceof SortPanelListener) {
            SortPanelListener l = (SortPanelListener) activity;
            mSortPanel = l.getSortPanel();
            mTextCounter = l.getCounter();
            mSortTitle = l.getSortTitle();
            mSortTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAppSettings.toggleLabelDescOrder();
                    LabelCache.invalidate();
                    EventBusHelper.updateDrawer();
                    scrollToTop();
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

        mPresenter.onCreateView(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        mEmptyView = null;
        mSortPanel = null;
        mTextCounter = null;
        mSortTitle = null;
        mRecyclerView = null;
        mPresenter.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_label_manager, menu);
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
        builder.setTitle(getString(R.string.action_sort));
        builder.setMenuRes(R.menu.sort_labels);
        builder.setChecked(menuItemFromOrder(mAppSettings.getLabelOrder()));
        builder.show(this, TAG_SORT_LABELS);
    }

    @Override
    public void onMenuItemClick(BottomMenuDialog dialog, String tag, MenuItem item) {
        switch (tag) {
            case TAG_ITEM_LABEL:
                handleLabelMenuItemClick(item, (Label) dialog.getData());
                break;

            case TAG_SORT_LABELS:
                handleSortMenuItemClick(item);
                break;
        }

        dialog.dismiss();
    }

    private void handleLabelMenuItemClick(MenuItem item, Label label) {
        switch (item.getItemId()) {
            case R.id.menu_rename:
                RenameLabelDialog.launch(getFragmentManager(), label);
                break;

            case R.id.menu_delete:
                DeleteLabelDialog.launch(getContext(), getFragmentManager(), label);
                break;
        }
    }

    private void handleSortMenuItemClick(MenuItem item) {
        LabelOrder order = orderFromMenuItem(item);
        mAppSettings.setLabelOrder(order);

        LabelCache.invalidate();
        EventBusHelper.updateDrawer();

        scrollToTop();
    }

    private int menuItemFromOrder(LabelOrder order) {
        if (order == null) {
            return -1;
        }
        HashMap<LabelOrder, Integer> map = new HashMap<>();
        map.put(LabelOrder.TITLE, R.id.menu_sort_title);
        map.put(LabelOrder.NOTE_COUNT, R.id.menu_sort_note_count);
        map.put(LabelOrder.CUSTOM, R.id.menu_sort_custom);
        return map.get(order);
    }

    private LabelOrder orderFromMenuItem(MenuItem item) {
        if (item == null) {
            return null;
        }
        SparseArray<LabelOrder> sparseArray = new SparseArray<>();
        sparseArray.put(R.id.menu_sort_title, LabelOrder.TITLE);
        sparseArray.put(R.id.menu_sort_note_count, LabelOrder.NOTE_COUNT);
        sparseArray.put(R.id.menu_sort_custom, LabelOrder.CUSTOM);
        return sparseArray.get(item.getItemId());
    }

    private void setSortText(LabelOrder order) {
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

    private void setSortPanelCounterText(int labelCount) {
        String s = getResources().getQuantityString(R.plurals.template_label_counter, labelCount, labelCount);
        ViewUtil.setText(mTextCounter, s);
    }

    @Override
    public void setItems(List<Label> items) {
        if (mAdapter != null) {
            mAdapter.setItems(items);
        }

        ViewUtil.show(items != null && items.size() == 0, mEmptyView);

        mNotEmptyList = items != null && items.size() > 0;
        showSortPanel(mNotEmptyList);
        if (items != null) {
            setSortPanelCounterText(items.size());
        }
        setSortText(mAppSettings.getLabelOrder());

        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void confirmDeleteDialog(Label label) {
        DeleteLabelDialog.launch((AppCompatActivity) getActivity(), label);
    }

    @Override
    public void startNoteListActivity(Label label) {
        NoteListActivity.launch(getContext(), label);
        mViewsLabelInteractor.execute(label).subscribe();
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
    public void addLabel(EventBusHelper.AddLabel e) {
        mPresenter.forceUpdate();
    }

    @Subscribe
    public void renameLabel(EventBusHelper.RenameLabel e) {
        mPresenter.forceUpdate();
    }

    @Subscribe
    public void deleteLabel(EventBusHelper.DeleteLabel e) {
        mPresenter.forceUpdate();
    }

    @Override
    public void scrollToTop() {
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    private interface ItemListener {

        void onItemClick(int pos, Label label);

        void onMenuItemClick(int pos, Label label);

    }

    private static class Adapter extends RecyclerView.Adapter<LabelManagerFragment.Adapter.ViewHolder> {

        private List<Label> mItems;

        private ItemListener mItemListener;

        private ItemTouchHelper mItemTouchHelper;

        private UpdateOrderLabelInteractor mOrderInteractor;

        Adapter(UpdateOrderLabelInteractor orderInteractor) {
            mOrderInteractor = orderInteractor;
            mItemTouchHelper = new ItemTouchHelper(new SimpleCallback(UP | DOWN, 0) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    swapItems(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                }

                @Override
                public boolean isLongPressDragEnabled() {
                    return true;
                }

                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    LabelCache.invalidate();

                    mOrderInteractor.execute(mItems).subscribe(new CompletableObserver() {
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

        void setItems(List<Label> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        void setItemListener(ItemListener l) {
            mItemListener = l;
        }

        Label getItem(int position) {
            if (position >= 0 && position < getItemCount()) {
                return mItems.get(position);
            }
            return null;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_label_manager, parent, false);
            return new ViewHolder(v, mItemListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Label label = getItem(position);
            setText(holder.mTextTitle, label.getTitle());

            long count = label.getNoteCount();
            setText(holder.mTextBadge, count <= 0 ? null : String.valueOf(count));
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

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
                mTextTitle = itemView.findViewById(R.id.text_title);
                mTextBadge = itemView.findViewById(R.id.text_badge);
                mMenu = itemView.findViewById(R.id.button_menu);
                mMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

package com.axel_stein.noteapp.label_manager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.label.UpdateLabelOrderInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.LabelCache;
import com.axel_stein.domain.model.LabelOrder;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.label.AddLabelDialog;
import com.axel_stein.noteapp.dialogs.label.DeleteLabelDialog;
import com.axel_stein.noteapp.dialogs.label.RenameLabelDialog;
import com.axel_stein.noteapp.main.NoteListActivity;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;
import static android.view.Gravity.END;
import static android.view.Gravity.TOP;
import static com.axel_stein.noteapp.utils.ViewUtil.setText;

public class LabelManagerFragment extends Fragment implements LabelManagerContract.View {

    private ItemListener mListener = new ItemListener() {
        @Override
        public void onItemClick(int pos, Label label) {
            mPresenter.onItemClick(pos, label);
        }

        @Override
        public void onMenuClick(int pos, Label label, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_rename:
                    RenameLabelDialog.launch(getFragmentManager(), label);
                    break;

                case R.id.menu_delete:
                    DeleteLabelDialog.launch(getContext(), getFragmentManager(), label);
                    break;
            }
        }
    };

    private Adapter mAdapter;

    private LabelManagerPresenter mPresenter = new LabelManagerPresenter();

    private View mEmptyView;

    @Inject
    AppSettingsRepository mSettingsRepository;

    @Inject
    UpdateLabelOrderInteractor mOrderInteractor;

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
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.empty_view);

        mAdapter = new Adapter(mOrderInteractor);
        mAdapter.setItemListener(mListener);
        mAdapter.attachRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mPresenter.onCreateView(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        mEmptyView = null;
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
        LabelOrder currentOrder = mSettingsRepository.getLabelOrder();
        if (currentOrder != null) {
            MenuItem item = menu.findItem(R.id.menu_sort);
            if (item != null) {
                MenuUtil.check(item.getSubMenu(), menuItemFromOrder(currentOrder), true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LabelOrder order = orderFromMenuItem(item);
        if (order != null) {
            item.setChecked(true);
            mSettingsRepository.setLabelOrder(order);
            LabelCache.invalidate();
            EventBusHelper.updateDrawer();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_add_label:
                AddLabelDialog.launch(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void setItems(List<Label> items) {
        if (mAdapter != null) {
            mAdapter.setItems(items);
        }
        ViewUtil.show(items != null && items.size() == 0, mEmptyView);
    }

    @Override
    public void confirmDeleteDialog(Label label) {
        DeleteLabelDialog.launch((AppCompatActivity) getActivity(), label);
    }

    @Override
    public void startNoteListActivity(Label label) {
        NoteListActivity.launch(getContext(), label);
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

    private interface ItemListener {

        void onItemClick(int pos, Label label);

        void onMenuClick(int pos, Label label, MenuItem item);

    }

    private static class Adapter extends RecyclerView.Adapter<LabelManagerFragment.Adapter.ViewHolder> {

        private List<Label> mItems;

        private ItemListener mItemListener;

        private ItemTouchHelper mItemTouchHelper;

        private UpdateLabelOrderInteractor mOrderInteractor;

        Adapter(UpdateLabelOrderInteractor orderInteractor) {
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
            final ViewHolder holder = new ViewHolder(v, mItemListener);
            holder.mDragHandler.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mItemTouchHelper.startDrag(holder);
                    return true;
                }
            });
            return holder;
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

            View mDragHandler;

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
                mDragHandler = itemView.findViewById(R.id.drag_handler);
                mTextTitle = itemView.findViewById(R.id.text_title);
                mTextBadge = itemView.findViewById(R.id.text_badge);
                mMenu = itemView.findViewById(R.id.button_menu);
                mMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu menu = new PopupMenu(v.getContext(), v, TOP | END, 0, R.style.NotebookPopupMenu);
                        menu.inflate(R.menu.item_label);
                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (l != null) {
                                    int pos = getAdapterPosition();
                                    if (pos >= 0 && pos < getItemCount()) {
                                        l.onMenuClick(pos, getItem(pos), item);
                                    }
                                }
                                return true;
                            }
                        });
                        menu.show();
                    }
                });
            }
        }

    }

}

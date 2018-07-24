package com.axel_stein.noteapp.notebook_manager;

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
import com.axel_stein.domain.interactor.notebook.UpdateNotebookOrderInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.model.NotebookCache;
import com.axel_stein.domain.model.NotebookOrder;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.ScrollableFragment;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.DeleteNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.RenameNotebookDialog;
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

public class NotebookManagerFragment extends Fragment implements NotebookManagerContract.View, ScrollableFragment {

    private ItemListener mListener = new ItemListener() {
        @Override
        public void onItemClick(int pos, Notebook notebook) {
            mPresenter.onItemClick(pos, notebook);
        }

        @Override
        public void onMenuClick(int pos, Notebook notebook, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_rename:
                    RenameNotebookDialog.launch(getFragmentManager(), notebook);
                    break;

                case R.id.menu_delete:
                    DeleteNotebookDialog.launch(getContext(), getFragmentManager(), notebook);
                    break;
            }
        }
    };

    private Adapter mAdapter;

    private NotebookManagerPresenter mPresenter = new NotebookManagerPresenter();

    private View mEmptyView;

    private RecyclerView mRecyclerView;

    @Inject
    AppSettingsRepository mSettingsRepository;

    @Inject
    UpdateNotebookOrderInteractor mOrderInteractor;

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
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.empty_view);

        mAdapter = new Adapter(mOrderInteractor);
        mAdapter.setItemListener(mListener);
        mAdapter.attachRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mPresenter.onCreateView(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        mEmptyView = null;
        mRecyclerView = null;
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
        NotebookOrder currentOrder = mSettingsRepository.getNotebookOrder();
        if (currentOrder != null) {
            MenuItem item = menu.findItem(R.id.menu_sort);
            if (item != null) {
                MenuUtil.check(item.getSubMenu(), menuItemFromOrder(currentOrder), true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NotebookOrder order = orderFromMenuItem(item);
        if (order != null) {
            item.setChecked(true);
            mSettingsRepository.setNotebookOrder(order);
            NotebookCache.invalidate();
            EventBusHelper.updateDrawer();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_add_notebook:
                AddNotebookDialog.launch(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void setItems(List<Notebook> items) {
        if (mAdapter != null) {
            mAdapter.setItems(items);
        }
        ViewUtil.show(items != null && items.size() == 0, mEmptyView);
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

    @Override
    public void scrollToTop() {
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    private interface ItemListener {

        void onItemClick(int pos, Notebook notebook);

        void onMenuClick(int pos, Notebook notebook, MenuItem item);

    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private List<Notebook> mItems;

        private ItemListener mItemListener;

        private ItemTouchHelper mItemTouchHelper;

        private UpdateNotebookOrderInteractor mOrderInteractor;

        Adapter(UpdateNotebookOrderInteractor orderInteractor) {
            mOrderInteractor = orderInteractor;
            mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(UP | DOWN, 0) {
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
                    NotebookCache.invalidate();
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
            Notebook notebook = getItem(position);
            setText(holder.mTextTitle, notebook.getTitle());

            long count = notebook.getNoteCount();
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
                        menu.inflate(R.menu.item_notebook);
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

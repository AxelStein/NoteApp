package com.axel_stein.noteapp.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notebook_manager.NotebookManagerActivity;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.noteapp.utils.ViewUtil.setText;

public class NotebooksFragment extends android.support.v4.app.Fragment {

    private NotebookItemListener mListener = new NotebookItemListener() {
        @Override
        public void onItemClick(int pos, Notebook notebook) {
            NoteListActivity.launch(getContext(), notebook);
        }
    };

    private Adapter mAdapter;

    private List<Notebook> mItems;

    private View mEmptyView;

    @Inject
    QueryNotebookInteractor mInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        EventBusHelper.subscribe(this);
        App.getAppComponent().inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_notebooks, menu);
        MenuUtil.tintMenuIconsAttr(getContext(), menu, R.attr.menuItemTintColor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_manage_notebooks:
                startActivity(new Intent(getContext(), NotebookManagerActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notebooks, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.empty_view);

        mAdapter = new Adapter();
        mAdapter.setItemListener(mListener);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (mItems == null) {
            forceUpdate();
        } else {
            setItems(mItems);
        }

        return view;
    }

    private void forceUpdate() {
        mInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Notebook>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<Notebook> notebooks) {
                        setItems(notebooks);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        mEmptyView = null;
        super.onDestroyView();
    }

    public void setItems(List<Notebook> items) {
        mItems = items;
        if (mAdapter != null) {
            mAdapter.setItems(items);
        }
        ViewUtil.show(items != null && items.size() == 0, mEmptyView);
    }

    @Subscribe
    public void updateDrawer(EventBusHelper.UpdateDrawer e) {
        forceUpdate();
    }

    @Subscribe
    public void onRecreate(EventBusHelper.Recreate e) {
        forceUpdate();
    }

    @Subscribe
    public void addNotebook(EventBusHelper.AddNotebook e) {
        forceUpdate();
    }

    @Subscribe
    public void renameNotebook(EventBusHelper.RenameNotebook e) {
        forceUpdate();
    }

    @Subscribe
    public void deleteNotebook(EventBusHelper.DeleteNotebook e) {
        forceUpdate();
    }

    private interface NotebookItemListener {

        void onItemClick(int pos, Notebook notebook);

    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private List<Notebook> mItems;

        private NotebookItemListener mNotebookItemListener;

        void setItems(List<Notebook> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        void setItemListener(NotebookItemListener l) {
            mNotebookItemListener = l;
        }

        Notebook getItem(int position) {
            if (position >= 0 && position < getItemCount()) {
                return mItems.get(position);
            }
            return null;
        }

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_notebook_list, parent, false);
            return new Adapter.ViewHolder(v, mNotebookItemListener);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            Notebook notebook = getItem(position);
            setText(holder.mTextNotebook, notebook.getTitle());

            long count = notebook.getNoteCount();
            setText(holder.mTextBadge, count <= 0 ? null : String.valueOf(count));
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView mTextNotebook;

            TextView mTextBadge;

            ViewHolder(View itemView, final NotebookItemListener l) {
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
                mTextNotebook = itemView.findViewById(R.id.text_notebook);
                mTextBadge = itemView.findViewById(R.id.text_badge);
            }
        }

    }

}

package com.axel_stein.noteapp.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.ScrollableFragment;
import com.axel_stein.noteapp.utils.ViewUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.noteapp.utils.ViewUtil.setText;

public class LabelsFragment extends Fragment implements ScrollableFragment {

    private ItemListener mListener = new ItemListener() {
        @Override
        public void onItemClick(int pos, Label label) {
            NoteListActivity.launch(getContext(), label);
        }
    };

    private Adapter mAdapter;

    private List<Label> mItems;

    private View mEmptyView;

    private RecyclerView mRecyclerView;

    @Inject
    QueryLabelInteractor mInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        EventBusHelper.subscribe(this);
        App.getAppComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_labels, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.empty_view);

        mAdapter = new Adapter();
        mAdapter.setItemListener(mListener);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
                .subscribe(new SingleObserver<List<Label>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<Label> items) {
                        setItems(items);
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
        mRecyclerView = null;
        super.onDestroyView();
    }

    public void setItems(List<Label> items) {
        mItems = items;
        if (mAdapter != null) {
            mAdapter.setItems(items);
        }
        ViewUtil.show(items != null && items.size() == 0, mEmptyView);
    }

    @Override
    public void scrollToTop() {
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(0);
        }
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
    public void addLabel(EventBusHelper.AddLabel e) {
        forceUpdate();
    }

    @Subscribe
    public void renameLabel(EventBusHelper.RenameLabel e) {
        forceUpdate();
    }

    @Subscribe
    public void deleteLabel(EventBusHelper.DeleteLabel e) {
        forceUpdate();
    }

    private interface ItemListener {

        void onItemClick(int pos, Label label);

    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private List<Label> mItems;

        private ItemListener mItemListener;

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
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_label_list, parent, false);
            return new Adapter.ViewHolder(v, mItemListener);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            Label item = getItem(position);
            ViewUtil.setText(holder.mTextNotebook, item.getTitle());

            long count = item.getNoteCount();
            setText(holder.mTextBadge, count <= 0 ? null : String.valueOf(count));
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView mTextNotebook;

            TextView mTextBadge;

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
                mTextNotebook = itemView.findViewById(R.id.text_notebook);
                mTextBadge = itemView.findViewById(R.id.text_badge);
            }
        }

    }

}

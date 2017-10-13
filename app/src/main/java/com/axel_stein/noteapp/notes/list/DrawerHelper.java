package com.axel_stein.noteapp.notes.list;

import android.support.annotation.Nullable;

import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

public class DrawerHelper {

    private static List<IDrawerItem> mItems;
    private static int mNotebookCount;
    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;
    @Inject
    QueryLabelInteractor mQueryLabelInteractor;
    private Callback mCallback;
    private SectionDrawerItem mNotebooks;
    private SectionDrawerItem mLabels;

    public DrawerHelper(Callback callback) {
        mCallback = callback;

        App.getAppComponent().inject(this);

        updateDrawerMenu(getSelectedItem(), mItems == null);
    }

    public void invalidate() {
        mItems = null;
        mNotebookCount = 0;
    }

    public void addNotebook(Notebook notebook) {
        updateImpl(true, false);
    }

    public void renameNotebook(Notebook notebook) {
        updateImpl(true, true);
    }

    public void deleteNotebook(Notebook notebook) {
        updateImpl(false, true);
    }

    public void addLabel(Label label) {
        updateImpl(true, false);
    }

    public void renameLabel(Label label) {
        updateImpl(true, true);
    }

    public void deleteLabel(Label label) {
        updateImpl(false, true);
    }

    @Nullable
    private IDrawerItem getSelectedItem() {
        if (mItems != null) {
            for (IDrawerItem drawerItem : mItems) {
                if (drawerItem.isSelected()) {
                    return drawerItem;
                }
            }
        }
        return null;
    }

    public void update(boolean saveSelection, boolean click) {
        updateImpl(saveSelection, click);
    }

    public int getNotebookCount() {
        return mNotebookCount;
    }

    private void updateImpl(boolean saveSelection, boolean click) {
        IDrawerItem selected = null;
        if (saveSelection) {
            selected = getSelectedItem();
        }

        invalidate();
        updateDrawerMenu(selected, click);
    }

    private void updateDrawerMenu(final IDrawerItem selected, final boolean click) {
        if (mItems != null) {
            if (mCallback != null) {
                mCallback.update(mItems, click, getSelectedItem());
            }
            return;
        }

        mItems = new ArrayList<>();

        mQueryNotebookInteractor.execute()
                .flatMap(new Function<List<Notebook>, SingleSource<List<Label>>>() {
                    @Override
                    public SingleSource<List<Label>> apply(@NonNull List<Notebook> notebooks) throws Exception {
                        mNotebookCount = notebooks.size();

                        mNotebooks = createSectionItem(R.string.action_notebooks, false);
                        mNotebooks.withIsExpanded(true);

                        mItems.add(mNotebooks);

                        for (int i = 0, count = notebooks.size(); i < count; i++) {
                            Notebook notebook = notebooks.get(i);

                            PrimaryDrawerItem item = createNotebookItem(notebook);
                            if (selected == null) {
                                item.withSetSelected(i == 0);
                            } else {
                                Object tag = selected.getTag();
                                if (notebook.equals(tag)) {
                                    item.withSetSelected(true);
                                }
                            }
                            item.withParent(mNotebooks);
                            mItems.add(item);
                        }

                        mItems.add(createAddItem(R.id.menu_add_notebook, R.string.action_add_notebook));

                        return mQueryLabelInteractor.execute();
                    }
                })
                .flatMapCompletable(new Function<List<Label>, CompletableSource>() {
                    @Override
                    public CompletableSource apply(@NonNull List<Label> labels) throws Exception {
                        mLabels = createSectionItem(R.string.action_labels, true);
                        mLabels.withIsExpanded(true);

                        mItems.add(mLabels);

                        for (Label label : labels) {
                            PrimaryDrawerItem item = createLabelItem(label);
                            item.withParent(mLabels);

                            if (selected != null) {
                                Object tag = selected.getTag();
                                if (label.equals(tag)) {
                                    item.withSetSelected(true);
                                }
                            }

                            mItems.add(item);
                        }

                        mItems.add(createAddItem(R.id.menu_add_label, R.string.action_add_label));

                        return Completable.complete();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (mCallback != null) {
                            mCallback.update(mItems, click, getSelectedItem());
                        }
                    }
                });
    }

    private SectionDrawerItem createSectionItem(int text, boolean divider) {
        return new SectionDrawerItem()
                .withDivider(divider)
                .withName(text);
    }

    private PrimaryDrawerItem createNotebookItem(Notebook notebook) {
        return createNotebookItem(notebook, notebook.getNoteCount());
    }

    private PrimaryDrawerItem createNotebookItem(Notebook notebook, long count) {
        return new PrimaryDrawerItem()
                .withIcon(R.drawable.ic_book_white_24dp)
                .withSelectedIconColorRes(R.color.color_accent)
                .withIconTintingEnabled(true)
                .withBadge(count > 0 ? String.valueOf(count) : null)
                .withTag(notebook)
                .withName(notebook.getTitle());
    }

    private PrimaryDrawerItem createLabelItem(Label label) {
        return new PrimaryDrawerItem()
                .withIcon(R.drawable.ic_label_white_24dp)
                .withSelectedIconColorRes(R.color.color_accent)
                .withIconTintingEnabled(true)
                .withBadge(label.getNoteCount() > 0 ? String.valueOf(label.getNoteCount()) : null)
                .withTag(label)
                .withName(label.getTitle());
    }

    private PrimaryDrawerItem createAddItem(long id, int text) {
        return new PrimaryDrawerItem()
                .withIdentifier(id)
                .withIcon(R.drawable.ic_add_white_24dp)
                .withIconTintingEnabled(true)
                .withSelectable(false)
                .withName(text);
    }

    public interface Callback {
        void update(List<IDrawerItem> items, boolean click, IDrawerItem selected);
    }
}


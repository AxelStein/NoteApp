package com.axel_stein.noteapp.widgets;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;

import java.util.List;

import javax.inject.Inject;

import static com.axel_stein.noteapp.widgets.WidgetProvider.NOTE_ID;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private List<Note> mItems;
    private String mPackageName;

    @Inject
    QueryNoteInteractor mQueryNoteInteractor;

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Inject
    AppSettingsRepository mSettings;

    private boolean mNightMode;

    public WidgetFactory(String packageName) {
        mPackageName = packageName;
        App.getAppComponent().inject(this);
        mNightMode = mSettings.nightMode();
    }

    @Override
    public void onCreate() {
        Log.d("TAG", "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        Log.d("TAG", "onDataSetChanged");
        mNightMode = mSettings.nightMode();

        List<Notebook> notebooks = mQueryNotebookInteractor.executeSync();
        mItems = mQueryNoteInteractor.executeSync(notebooks.get(0));
    }

    @Override
    public int getCount() {
        Log.d("TAG", "getCount " + (mItems == null ? 0 : mItems.size()));
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Note note = mItems.get(position);

        RemoteViews view = new RemoteViews(mPackageName, mNightMode ? R.layout.item_widget_note_night : R.layout.item_widget_note);
        view.setTextViewText(R.id.text_note, note.getTitle());

        Intent intent = new Intent();
        intent.putExtra(NOTE_ID, note.getId());
        view.setOnClickFillInIntent(R.id.item, intent);

        return view;
    }

    @Override
    public void onDestroy() {
        Log.d("TAG", "onDestroy");
    }

}

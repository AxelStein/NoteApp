package com.axel_stein.noteapp.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.notes.list.SearchActivity;

import javax.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class WidgetProvider extends AppWidgetProvider {

    public static final String NOTE_ID = "com.axel_stein.noteapp.NOTE_ID";
    private static final String ACTION_NOTE_CLICK = "com.axel_stein.noteapp.ACTION_NOTE_CLICK";

    @Inject
    AppSettingsRepository mSettings;

    private boolean mNightMode;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        App.getAppComponent().inject(this);
        mNightMode = mSettings.nightMode();
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), mNightMode ? R.layout.layout_widget_night : R.layout.layout_widget);
        onAddButtonClick(context, rv);
        onSearchButtonClick(context, rv);

        setList(rv, context, appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
    }

    private void onAddButtonClick(Context context, RemoteViews rv) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.button_add_note, pendingIntent);
    }

    private void onSearchButtonClick(Context context, RemoteViews rv) {
        Intent intent = new Intent(context, SearchActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.button_search, pendingIntent);
    }

    private void setList(RemoteViews rv, Context context, int appWidgetId) {
        Intent adapter = new Intent(context, WidgetService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        rv.setRemoteAdapter(R.id.list_view, adapter);

        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(ACTION_NOTE_CLICK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        rv.setPendingIntentTemplate(R.id.list_view, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_NOTE_CLICK)) {
                long noteId = intent.getLongExtra(NOTE_ID, -1);
                if (noteId != -1) {
                    Intent editIntent = new Intent(context, EditNoteActivity.class);
                    editIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    editIntent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, noteId);
                    context.startActivity(editIntent);
                }
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

}

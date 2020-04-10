package com.axel_stein.noteapp.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.App;

import javax.inject.Inject;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String ACTION_START = "com.axel_stein.noteapp:ACTION_START";
    public static final String ACTION_SHOW_NOTIFICATION = "com.axel_stein.noteapp:ACTION_SHOW_NOTIFICATION";
    public static final String EXTRA_NOTE = "EXTRA_NOTE";

    public static Intent getLaunchIntent(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_START);
        return intent;
    }

    public static Intent getShowNotificationIntent(Context context, Note note) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(EXTRA_NOTE, note);
        return intent;
    }

    @Inject
    AndroidNotificationTray mAndroidNotificationTray;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (intent.getAction() == null) return;

        App.getAppComponent().inject(this);

        String action = intent.getAction();
        switch (action) {
            case ACTION_START:
                ReminderService.launch(context);
                break;

            case ACTION_SHOW_NOTIFICATION:
                Note note = (Note) intent.getSerializableExtra(EXTRA_NOTE);
                if (note != null && note.hasId() && !note.isTrashed() && note.hasReminder()) {
                    mAndroidNotificationTray.showNotification(note);
                }
                break;
        }
    }
}

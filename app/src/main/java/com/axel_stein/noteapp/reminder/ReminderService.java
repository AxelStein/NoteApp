package com.axel_stein.noteapp.reminder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.interactor.reminder.GetReminderInteractor;
import com.axel_stein.domain.interactor.reminder.UpdateReminderInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Reminder;
import com.axel_stein.noteapp.App;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.util.List;

import javax.inject.Inject;

public class ReminderService extends IntentService {
    public static final String ACTION_START = "com.axel_stein.noteapp.reminder.ACTION_START";
    public static final String ACTION_REBOOT = "com.axel_stein.noteapp.reminder.ACTION_REBOOT";

    public static void launch(Context context) {
        Intent intent = new Intent(context, ReminderService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    @Inject
    GetNoteInteractor mGetNoteInteractor;

    @Inject
    GetReminderInteractor mGetReminderInteractor;

    @Inject
    UpdateReminderInteractor mUpdateReminderInteractor;

    @Inject
    ReminderScheduler mReminderScheduler;

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        if (intent.getAction() == null) return;

        App.getAppComponent().inject(this);

        String action = intent.getAction();
        switch (action) {
            case ACTION_START: {
                MutableDateTime now = new MutableDateTime();
                now.setMillisOfSecond(0);

                List<Reminder> list = mGetReminderInteractor.queryDateTime(now.toDateTime());
                for (Reminder r : list) {
                    Note note = mGetNoteInteractor.get(r.getNoteId(), false);
                    sendBroadcast(ReminderReceiver.getShowNotificationIntent(this, note));
                    if (r.getRepeatMode() != Reminder.REPEAT_MODE_NONE) {
                        r.moveDateTime();
                        mUpdateReminderInteractor.executeSync(r);
                    }
                }
                //mReminderScheduler.schedule(mGetReminderInteractor.getNextAfter(now.toDateTime()));
                break;
            }

            case ACTION_REBOOT:
                List<Reminder> list = mGetReminderInteractor.query();
                for (Reminder reminder : list) {
                    DateTime dateTime = reminder.getDateTime();
                    if (dateTime.isAfterNow()) {
                        mReminderScheduler.schedule(reminder);
                    }
                }
                break;
        }
    }

}

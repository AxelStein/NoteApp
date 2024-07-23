package com.axel_stein.noteapp.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.AlarmManagerCompat;

import com.axel_stein.domain.model.Reminder;

import java.util.Objects;

import static android.app.AlarmManager.RTC_WAKEUP;
import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class ReminderScheduler {
    private final Context mContext;
    private final AlarmManager mAlarmManager;

    public ReminderScheduler(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void schedule(Reminder reminder) {
        if (reminder != null) {
            schedule(reminder.getNoteId(), reminder.getDateTime().getMillis());
        }
    }

    private void schedule(String noteId, long timestamp) {
        if (isEmpty(noteId)) return;
        if (timestamp < System.currentTimeMillis()) return;

        Intent intent = ReminderReceiver.getLaunchIntent(mContext);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, Objects.hashCode(noteId), intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManagerCompat.setExactAndAllowWhileIdle(mAlarmManager, RTC_WAKEUP, timestamp, pi);
    }

    public void cancel(String noteId) {
        Intent intent = ReminderReceiver.getLaunchIntent(mContext);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, Objects.hashCode(noteId), intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        mAlarmManager.cancel(pi);
    }

    /*
        if (SDK_INT >= LOLLIPOP) {
            AlarmManager.AlarmClockInfo nextAlarm = mAlarmManager.getNextAlarmClock();
            boolean setNextAlarm = false;
            if (nextAlarm != null) {
                long nextAlarmTime = nextAlarm.getTriggerTime();
                setNextAlarm = nextAlarmTime > timestamp;
            }
            if (nextAlarm == null || setNextAlarm) {
                mAlarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(timestamp, pi), pi);
            }
        } else {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pi);
        }
        */

    /*
        if (SDK_INT >= M) {
            mAlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, timestamp, pi);
        } else {
            mAlarmManager.setExact(RTC_WAKEUP, timestamp, pi);
        }
        */

     /*
    public static void cancelAlarm(Context context) {
        Intent i = new Intent(context, AlarmReceiver.class);
        i.setAction(ACTION_FETCH);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(pi);
        }
    }
    */


}

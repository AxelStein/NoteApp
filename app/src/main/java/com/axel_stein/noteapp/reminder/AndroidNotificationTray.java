package com.axel_stein.noteapp.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.R;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.N;
import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class AndroidNotificationTray {
    private static final String APP_ID = "com.axel_stein.noteapp:";
    private static final String CHANNEL_ID = APP_ID + "CHANNEL_ID";
    private static final String CHANNEL_NAME = "Reminders";
    private static final String CHANNEL_DESCRIPTION = "Note reminders";
    private static final String GROUP_ID = APP_ID + "GROUP_ID";

    private Context mContext;
    private PendingIntentFactory mPendingIntents;

    public AndroidNotificationTray(Context context) {
        mContext = context;
        mPendingIntents = new PendingIntentFactory(context);
    }

    public void showNotification(Note note) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        Notification notification = buildNotification(note);
        createNotificationChannel();
        notificationManager.notify(note.hashCode(), notification);
        if (SDK_INT >= N) {
            notificationManager.notify(0, buildSummaryNotification());
        }
        wakeLock();
    }

    private void wakeLock() {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl;
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.axel_stein.noteapp:reminder");
            wl.acquire(3000);
        }
    }

    private Notification buildNotification(Note note) {
        String title = note.getTitle();
        String content = note.getContent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_none_24dp)
                .setContentTitle(isEmpty(title) ? content : title)
                .setContentText(isEmpty(title) ? null : content)
                .setStyle(new NotificationCompat.InboxStyle())
                .setGroup(GROUP_ID)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(mPendingIntents.showNote(note))
                .setAutoCancel(true)
                .setVibrate(new long[]{500,500,500,500,500,500,500,500})
                .setPriority(NotificationCompat.PRIORITY_MAX);
        return builder.build();
    }

    private Notification buildSummaryNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_none_24dp)
                .setGroup(GROUP_ID)
                .setGroupSummary(true);
        return builder.build();
    }

    private void createNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

}

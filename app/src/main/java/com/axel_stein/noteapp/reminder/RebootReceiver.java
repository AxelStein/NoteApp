package com.axel_stein.noteapp.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent service = new Intent(context, ReminderService.class);
            service.setAction(ReminderService.ACTION_REBOOT);
            context.startService(service);
        }
    }
}

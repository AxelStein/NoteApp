package com.axel_stein.noteapp.google_drive;

import android.util.Log;

import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEventService;

public class GoogleDriveService extends DriveEventService {

    @Override
    public void onChange(ChangeEvent changeEvent) {
        super.onChange(changeEvent);
        // fixme
        Log.wtf("TAG", "GoogleDriveService onChange = " + changeEvent);
    }

}

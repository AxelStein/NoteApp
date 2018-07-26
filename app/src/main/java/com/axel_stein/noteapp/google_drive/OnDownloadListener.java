package com.axel_stein.noteapp.google_drive;

import android.support.annotation.Nullable;

import com.google.android.gms.drive.DriveFile;

public interface OnDownloadListener {
    void onDownload(@Nullable DriveFile file);
}

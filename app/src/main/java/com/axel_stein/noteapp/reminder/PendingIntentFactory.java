package com.axel_stein.noteapp.reminder;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.TaskStackBuilder;

import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.main.edit.EditNoteActivity;

import java.util.Objects;

public class PendingIntentFactory {
    private final Context mContext;

    public PendingIntentFactory(Context context) {
        mContext = context;
    }

    public PendingIntent showNote(Note note) {
        Intent resultIntent = new Intent(mContext, EditNoteActivity.class);
        resultIntent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.getId());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        return stackBuilder.getPendingIntent(Objects.hashCode(note.getId()), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

}

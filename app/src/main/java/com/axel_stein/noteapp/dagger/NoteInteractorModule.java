package com.axel_stein.noteapp.dagger;

import android.text.format.DateFormat;

import androidx.core.os.ConfigurationCompat;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.reminder.SqlReminderRepository;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.EmptyTrashInteractor;
import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.interactor.note.InsertNoteInteractor;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.interactor.note.SetArchivedNoteInteractor;
import com.axel_stein.domain.interactor.note.SetNotebookNoteInteractor;
import com.axel_stein.domain.interactor.note.SetPinnedNoteInteractor;
import com.axel_stein.domain.interactor.note.SetStarredNoteInteractor;
import com.axel_stein.domain.interactor.note.SetTrashedNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteInteractor;
import com.axel_stein.noteapp.App;

import java.util.Locale;

import dagger.Module;
import dagger.Provides;

@Module
class NoteInteractorModule {

    @Provides
    SetNotebookNoteInteractor updateNotebook(SqlNoteRepository r) {
        return new SetNotebookNoteInteractor(r);
    }

    @Provides
    DeleteNoteInteractor delete(SqlNoteRepository n, SqlReminderRepository r) {
        return new DeleteNoteInteractor(n, r);
    }

    @Provides
    EmptyTrashInteractor emptyTrash(QueryNoteInteractor q, DeleteNoteInteractor d) {
        return new EmptyTrashInteractor(q, d);
    }

    @Provides
    GetNoteInteractor get(SqlNoteRepository n) {
        return new GetNoteInteractor(n);
    }

    @Provides
    InsertNoteInteractor insert(SqlNoteRepository r) {
        return new InsertNoteInteractor(r);
    }

    @Provides
    QueryNoteInteractor query(SqlNoteRepository n, AppSettingsRepository s, SqlReminderRepository r, App app) {
        Locale locale = ConfigurationCompat.getLocales(app.getResources().getConfiguration()).get(0);
        return new QueryNoteInteractor(n, s, r, locale, DateFormat.is24HourFormat(app));
    }

    @Provides
    SetTrashedNoteInteractor trash(SqlNoteRepository r) {
        return new SetTrashedNoteInteractor(r);
    }

    @Provides
    UpdateNoteInteractor update(SqlNoteRepository r) {
        return new UpdateNoteInteractor(r);
    }

    @Provides
    SetPinnedNoteInteractor pin(SqlNoteRepository r) {
        return new SetPinnedNoteInteractor(r);
    }

    @Provides
    SetStarredNoteInteractor star(SqlNoteRepository r) {
        return new SetStarredNoteInteractor(r);
    }

    @Provides
    SetArchivedNoteInteractor archive(SqlNoteRepository r) {
        return new SetArchivedNoteInteractor(r);
    }

}

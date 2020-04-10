package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.reminder.SqlReminderRepository;
import com.axel_stein.domain.interactor.reminder.DeleteReminderInteractor;
import com.axel_stein.domain.interactor.reminder.GetReminderInteractor;
import com.axel_stein.domain.interactor.reminder.InsertReminderInteractor;
import com.axel_stein.domain.interactor.reminder.UpdateReminderInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class ReminderInteractorModule {

    @Provides
    InsertReminderInteractor insert(SqlReminderRepository r, SqlNoteRepository n) {
        return new InsertReminderInteractor(r, n);
    }

    @Provides
    UpdateReminderInteractor update(SqlReminderRepository r) {
        return new UpdateReminderInteractor(r);
    }

    @Provides
    DeleteReminderInteractor delete(SqlReminderRepository r, SqlNoteRepository n) {
        return new DeleteReminderInteractor(r, n);
    }

    @Provides
    GetReminderInteractor get(SqlReminderRepository r, SqlNoteRepository n) {
        return new GetReminderInteractor(r, n);
    }

}

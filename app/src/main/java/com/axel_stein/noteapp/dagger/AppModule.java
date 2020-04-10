package com.axel_stein.noteapp.dagger;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.axel_stein.data.AppDatabase;
import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.notebook.SqlNotebookRepository;
import com.axel_stein.data.reminder.SqlReminderRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.google_drive.DriveServiceHelper;
import com.axel_stein.noteapp.reminder.AndroidNotificationTray;
import com.axel_stein.noteapp.reminder.ReminderScheduler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final App mApp;

    public AppModule(App app) {
        mApp = app;
    }

    @Provides
    @Singleton
    App provideApp() {
        return mApp;
    }

    @Provides
    @Singleton
    AppDatabase provideDatabase(App app) {
        return Room.databaseBuilder(app, AppDatabase.class, app.getPackageName())
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3,
                        AppDatabase.MIGRATION_3_4)
                .build();
    }

    @Provides
    @Singleton
    SqlReminderRepository provideReminderRepository(AppDatabase db) {
        return new SqlReminderRepository(db.reminderDao());
    }

    @Provides
    @Singleton
    SqlNotebookRepository provideNotebookRepository(AppDatabase db) {
        return new SqlNotebookRepository(db.notebookDao());
    }

    @Provides
    @Singleton
    SqlNoteRepository provideNoteRepository(AppDatabase db) {
        return new SqlNoteRepository(db.noteDao());
    }

    @Provides
    AppSettingsRepository provideSettings(App app) {
        return new AppSettingsRepository(PreferenceManager.getDefaultSharedPreferences(app));
    }

    @Provides
    DriveServiceHelper provideDriveService(App app) {
        return new DriveServiceHelper(app);
    }

    @Provides
    ReminderScheduler provideReminderScheduler(App app) {
        return new ReminderScheduler(app);
    }

    @Provides
    AndroidNotificationTray provideAndroidNotificationTray(App app) {
        return new AndroidNotificationTray(app);
    }

}

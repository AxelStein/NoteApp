package com.axel_stein.noteapp.dagger;

import android.arch.persistence.room.Room;
import android.support.v7.preference.PreferenceManager;

import com.axel_stein.data.AppDatabase;
import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.label.SqlLabelRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.note_label_pair.SqlNoteLabelPairRepository;
import com.axel_stein.data.notebook.SqlNotebookRepository;
import com.axel_stein.domain.interactor.ResetInteractor;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.axel_stein.data.AppDatabase.MIGRATION_1_2;
import static com.axel_stein.data.AppDatabase.MIGRATION_2_3;
import static com.axel_stein.data.AppDatabase.MIGRATION_3_4;

@Module
public class AppModule {
    private App mApp;

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
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .build();
    }

    @Provides
    @Singleton
    SqlLabelRepository provideLabelRepository(AppDatabase db) {
        return new SqlLabelRepository(db.labelDao());
    }

    @Provides
    @Singleton
    SqlNoteLabelPairRepository provideLabelHelperRepository(AppDatabase db) {
        return new SqlNoteLabelPairRepository(db.labelHelperDao());
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
        return new AppSettingsRepository(PreferenceManager.getDefaultSharedPreferences(app), app.getString(R.string.default_notebook));
    }

    @Provides
    ResetInteractor provideReset(SqlNoteRepository notes,
                                 SqlNotebookRepository notebooks,
                                 SqlLabelRepository labels,
                                 SqlNoteLabelPairRepository labelHelper) {
        return new ResetInteractor(notes, notebooks, labels, labelHelper);
    }

}

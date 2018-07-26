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
import com.axel_stein.noteapp.google_drive.GoogleDriveInteractor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
        return Room.databaseBuilder(app, AppDatabase.class, app.getPackageName()).build();
    }

    @Provides
    @Singleton
    GoogleDriveInteractor provideGoogleDriveInteractor(App app,
                                                       SqlNoteRepository n,
                                                       SqlNotebookRepository b,
                                                       SqlLabelRepository l,
                                                       SqlNoteLabelPairRepository p,
                                                       AppSettingsRepository s) {
        return new GoogleDriveInteractor(app, n, b, l, p, s);
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
        return new AppSettingsRepository(PreferenceManager.getDefaultSharedPreferences(app));
    }

    @Provides
    ResetInteractor provideReset(SqlNoteRepository notes,
                                 SqlNotebookRepository notebooks,
                                 SqlLabelRepository labels,
                                 SqlNoteLabelPairRepository labelHelper) {
        return new ResetInteractor(notes, notebooks, labels, labelHelper);
    }

}

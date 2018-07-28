package com.axel_stein.noteapp;

import android.app.Application;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.dagger.AppComponent;
import com.axel_stein.noteapp.dagger.AppModule;
import com.axel_stein.noteapp.dagger.DaggerAppComponent;

public class App extends Application {

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Notebook.TITLE_ALL = getString(R.string.notebook_all);
        Notebook.ICON_ALL = R.drawable.baseline_horizontal_split_white_24;

        Notebook.TITLE_STARRED = getString(R.string.notebook_starred);
        Notebook.ICON_STARRED = R.drawable.ic_star_white_24dp;

        Notebook.TITLE_INBOX = getString(R.string.action_inbox);
        Notebook.ICON_INBOX = R.drawable.ic_inbox_white_24dp;

        sAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }

}

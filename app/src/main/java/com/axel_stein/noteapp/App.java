package com.axel_stein.noteapp;

import androidx.multidex.MultiDexApplication;

import com.axel_stein.noteapp.dagger.AppComponent;
import com.axel_stein.noteapp.dagger.AppModule;
import com.axel_stein.noteapp.dagger.DaggerAppComponent;

public class App extends MultiDexApplication {

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        sAppComponent.inject(this);
    }

}

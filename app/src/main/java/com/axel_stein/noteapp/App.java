package com.axel_stein.noteapp;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDexApplication;

import com.axel_stein.noteapp.dagger.AppComponent;
import com.axel_stein.noteapp.dagger.AppModule;
import com.axel_stein.noteapp.dagger.DaggerAppComponent;
import com.google.android.gms.ads.MobileAds;

import javax.inject.Inject;

import timber.log.Timber;

public class App extends MultiDexApplication implements DefaultLifecycleObserver {

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Inject
    public BillingManager billingManager;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        sAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        sAppComponent.inject(this);

        MobileAds.initialize(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        billingManager.onStart();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        billingManager.onStop();
    }
}

package com.axel_stein.noteapp.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import javax.inject.Inject;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    @Inject
    public AppSettingsRepository mAppSettings;

    protected boolean mNightMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent().inject(this);

        mNightMode = mAppSettings.nightModeEnabled();
        setTheme(mNightMode ? R.style.AppThemeDark : R.style.AppTheme);

        super.onCreate(savedInstanceState);
    }

    protected void setupAds() {
        AdView adView = findViewById(R.id.adView);
        View adDivider = findViewById(R.id.adDivider);
        if (adView == null) return;

        if (mAppSettings.adsEnabled()) {
            adView.setVisibility(View.VISIBLE);
            if (adDivider != null) {
                adDivider.setVisibility(View.VISIBLE);
            }
            adView.loadAd(
                new AdRequest.Builder()
                    .build()
            );
        } else {
            adView.setVisibility(View.GONE);
            if (adDivider != null) {
                adDivider.setVisibility(View.GONE);
            }
        }
    }

    protected void setFragment(@Nullable Fragment fragment, String tag) {
        if (fragment == null) {
            return;
        }

        try {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.content, fragment, tag)
                    .commit();
        } catch (Exception e) {
            // Catch IllegalStateException: Can not perform this action after onSaveInstanceState
            e.printStackTrace();
        }
    }

    protected boolean hasFragment(String tag) {
        try {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.findFragmentByTag(tag) != null) {
                return true;
            }
        } catch (Exception e) {
            // Catch IllegalStateException: Can not perform this action after onSaveInstanceState
            e.printStackTrace();
        }
        return false;
    }

    protected Fragment getFragment(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentByTag(tag);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

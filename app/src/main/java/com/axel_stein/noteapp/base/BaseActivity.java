package com.axel_stein.noteapp.base;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.axel_stein.noteapp.settings.SettingsActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import javax.inject.Inject;

import timber.log.Timber;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements AppSettingsRepository.OnEnableAdsListener {

    @Inject
    public AppSettingsRepository mAppSettings;

    protected boolean mNightMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent().inject(this);

        mNightMode = mAppSettings.nightModeEnabled();
        setTheme(mNightMode ? R.style.AppThemeDark : R.style.AppTheme);

        super.onCreate(savedInstanceState);

        mAppSettings.addOnEnableAdsListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppSettings.removeOnEnableAdsListener(this);
    }

    protected void setupAds() {
        enableAds(mAppSettings.adsEnabled());
    }

    private void enableAds(boolean enable) {
        AdView adView = findViewById(R.id.adView);
        View adDivider = findViewById(R.id.adDivider);
        View adProposal = findViewById(R.id.adProposal);
        if (adView == null) return;

        Timber.d("enableAds=%s", enable);

        if (enable) {
            if (adProposal != null) {
                adProposal.setVisibility(mAppSettings.adProposalEnabled() ? View.VISIBLE : View.GONE);
                adProposal.setOnClickListener(v -> {
                    mAppSettings.setAdProposalEnabled(false);
                    adProposal.setVisibility(View.GONE);
                    startActivity(new Intent(this, SettingsActivity.class));
                });
            }

            adView.setVisibility(View.VISIBLE);
            if (adDivider != null) {
                adDivider.setVisibility(View.VISIBLE);
            }
            adView.loadAd(
                new AdRequest.Builder()
                    .build()
            );
        } else {
            if (adProposal != null) {
                adProposal.setVisibility(View.GONE);
            }
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

    @Override
    public void onEnableAds(boolean enable) {
        enableAds(enable);
    }
}

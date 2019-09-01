package com.axel_stein.noteapp.settings;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        EventBusHelper.subscribe(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Subscribe
    public void showMessage(EventBusHelper.Message e) {
        if (e.hasMsgRes()) {
            showMessage(e.getMsgRes(), e.getDelay());
        } else {
            showMessage(e.getMsg(), e.getDelay());
        }
    }

    private void showMessage(int msgRes, int delay) {
        showMessage(getString(msgRes), delay);
    }

    private void showMessage(final String msg, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Snackbar.make(findViewById(R.id.coordinator_settings), msg, Snackbar.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                }
            }
        }, delay == 0 ? 100 : delay);
    }

    @Subscribe
    public void onRecreate(EventBusHelper.Recreate e) {
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

}

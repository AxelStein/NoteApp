package com.axel_stein.noteapp.backup;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.utils.MenuUtil;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BackupActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        ButterKnife.bind(this);
        EventBusHelper.subscribe(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_backup, menu);
        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        return super.onCreateOptionsMenu(menu);
    }

    @Subscribe
    public void showMessage(EventBusHelper.Message e) {
        if (e.hasMsgRes()) {
            showMessage(e.getMsgRes());
        } else {
            showMessage(e.getMsg());
        }
    }

    private void showMessage(final String msg) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Snackbar.make(findViewById(R.id.coordinator_backup), msg, Snackbar.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                }
            }
        }, 100);
    }

    private void showMessage(int msgRes) {
        showMessage(getString(msgRes));
    }

    @Override
    protected void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

}

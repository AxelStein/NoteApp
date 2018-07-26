package com.axel_stein.noteapp.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.google_drive.GoogleDriveInteractor;
import com.axel_stein.noteapp.google_drive.OnSignOutListener;
import com.axel_stein.noteapp.google_drive.UserData;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.user_photo)
    ImageView mUserPhoto;

    @BindView(R.id.user_name)
    TextView mUserName;

    @BindView(R.id.user_email)
    TextView mUserEmail;

    @BindView(R.id.button_import)
    Button mImport;

    @BindView(R.id.button_sign_out)
    Button mSignOut;

    @Inject
    AppSettingsRepository mAppSettings;

    @Inject
    GoogleDriveInteractor mGoogleDrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        App.getAppComponent().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setUserData(mGoogleDrive.getUserData());

        mImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleDrive.importAndReplace();
            }
        });

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleDrive.signOut(new OnSignOutListener() {
                    @Override
                    public void onSuccess() {
                        EventBusHelper.signOut();
                        finish();
                    }
                });
            }
        });
    }

    private void setUserData(UserData user) {
        if (user == null) {
            return;
        }

        Uri photo = user.getPhoto();
        String name = user.getName();
        String email = user.getEmail();

        boolean nightMode = mAppSettings.nightMode();
        int icon = nightMode ? R.drawable.ic_account_circle_white_36dp : R.drawable.ic_account_circle_grey_36dp;

        if (photo == null) {
            mUserPhoto.setImageResource(icon);
        } else {
            Picasso.get().load(photo).placeholder(icon).into(mUserPhoto);
        }

        ViewUtil.setText(mUserName, name);

        ViewUtil.setText(mUserEmail, email);
        ViewUtil.show(!TextUtils.isEmpty(email), mUserEmail);
    }

}

package com.axel_stein.noteapp.main;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.axel_stein.domain.interactor.reminder.DeleteReminderInteractor;
import com.axel_stein.domain.interactor.reminder.GetReminderInteractor;
import com.axel_stein.domain.interactor.reminder.InsertReminderInteractor;
import com.axel_stein.domain.interactor.reminder.UpdateReminderInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Reminder;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.reminder.ReminderScheduler;
import com.axel_stein.noteapp.utils.DateFormatter;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ResourceUtil;
import com.axel_stein.noteapp.utils.SimpleCompletableObserver;
import com.axel_stein.noteapp.utils.SimpleSingleObserver;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

import org.joda.time.MutableDateTime;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class AddReminderActivity extends BaseActivity {
    private static final String EXTRA_NOTE_ID = "com.axel_stein.noteapp.EXTRA_NOTE_ID";
    private static final String EXTRA_YEAR = "EXTRA_YEAR";
    private static final String EXTRA_MONTH = "EXTRA_MONTH";
    private static final String EXTRA_DAY = "EXTRA_DAY";
    private static final String EXTRA_HOUR = "EXTRA_HOUR";
    private static final String EXTRA_MINUTE = "EXTRA_MINUTE";
    private static final String EXTRA_REPEAT_MODE = "EXTRA_REPEAT_MODE";
    private static final String EXTRA_REPEAT_COUNT = "EXTRA_REPEAT_COUNT";

    public static void launch(Context context, @NonNull Note note) {
        Intent intent = new Intent(context, AddReminderActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, note.getId());
        context.startActivity(intent);
    }

    private Toolbar mToolbar;
    private TextView mTextDate;
    private TextView mTextTime;
    private TextView mTextRepeat;
    private TextView mTextRepeatEnd;
    private View mEditRepeatLayout;
    private EditText mEditRepeatCount;
    private TextView mTextRepeatPeriod;
    private int mRepeatMode;
    private int mRepeatCount = 1;
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    @Nullable
    private Reminder mReminder;

    @Nullable
    private MutableDateTime mDateTime;

    @Nullable
    private String mNoteId;

    @Inject
    InsertReminderInteractor mInsertReminderInteractor;

    @Inject
    UpdateReminderInteractor mUpdateReminderInteractor;

    @Inject
    GetReminderInteractor mGetReminderInteractor;

    @Inject
    DeleteReminderInteractor mDeleteReminderInteractor;

    @Inject
    ReminderScheduler mReminderScheduler;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);

        setContentView(R.layout.activity_add_reminder);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationIcon(ResourceUtil.getDrawableTinted(this,
                R.drawable.ic_clear_white_24dp, R.attr.menuItemTintColor));

        mTextDate = findViewById(R.id.text_date);
        mTextDate.setOnClickListener(v -> showDatePicker());

        mTextTime = findViewById(R.id.text_time);
        mTextTime.setOnClickListener(v -> showTimePicker());

        mTextRepeat = findViewById(R.id.text_repeat);
        mTextRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectRepeatModeDialog();
            }
        });

        mTextRepeatEnd = findViewById(R.id.text_repeat_end);
        mTextRepeatEnd.setOnClickListener(v -> {
            // todo
        });

        mEditRepeatLayout = findViewById(R.id.layout_edit_repeat);
        mEditRepeatLayout.setOnClickListener(v -> showSelectRepeatModeDialog());
        mEditRepeatCount = findViewById(R.id.edit_repeat_count);
        mEditRepeatCount.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    mRepeatCount = Integer.valueOf(s.toString());
                } catch (Exception ignored) {
                }
            }
        });
        mTextRepeatPeriod = findViewById(R.id.text_repeat_period);

        Intent intent = getIntent();
        mNoteId = intent.getStringExtra(EXTRA_NOTE_ID);

        mGetReminderInteractor.findByNoteId(mNoteId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleSingleObserver<Reminder>() {
                    @Override
                    public void onSuccess(Reminder reminder) {
                        if (reminder.hasId()) {
                            mReminder = reminder;
                            if (mToolbar != null) {
                                mToolbar.setTitle(R.string.action_edit_reminder);
                            }
                        }

                        mDateTime = new MutableDateTime(System.currentTimeMillis());
                        if (savedInstanceState != null) {
                            mDateTime.setYear(savedInstanceState.getInt(EXTRA_YEAR));
                            mDateTime.setMonthOfYear(savedInstanceState.getInt(EXTRA_MONTH));
                            mDateTime.setDayOfMonth(savedInstanceState.getInt(EXTRA_DAY));
                            mDateTime.setHourOfDay(savedInstanceState.getInt(EXTRA_HOUR));
                            mDateTime.setMinuteOfHour(savedInstanceState.getInt(EXTRA_MINUTE));

                            mRepeatMode = savedInstanceState.getInt(EXTRA_REPEAT_MODE);
                            mRepeatCount = savedInstanceState.getInt(EXTRA_REPEAT_COUNT);
                        } else if (mReminder == null) {
                            int minutes = mDateTime.getMinuteOfHour();
                            if (minutes < 30) {
                                mDateTime.setMinuteOfHour(30);
                            } else {
                                mDateTime.addHours(1);
                                mDateTime.setMinuteOfHour(0);
                            }
                        } else {
                            mDateTime = new MutableDateTime(mReminder.getDateTime());
                            mRepeatMode = mReminder.getRepeatMode();
                            mRepeatCount = mReminder.getRepeatCount();
                        }

                        ViewUtil.setText(mTextDate,
                                DateFormatter.formatDate(getApplication(), mDateTime.getMillis(), true));
                        ViewUtil.setText(mTextTime,
                                DateFormatter.formatTime(getApplication(), mDateTime.getMillis()));
                        setViewRepeatMode(mRepeatMode, mRepeatCount);

                        supportInvalidateOptionsMenu();
                    }
                });

        setupAds();
    }

    private void showSelectRepeatModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_repeat);
        final String[] items = getResources().getStringArray(R.array.repeat_mode);
        builder.setItems(items, (dialog, which) -> setViewRepeatMode(which, mRepeatCount));
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setViewRepeatMode(int repeatMode, int repeatCount) {
        mRepeatMode = repeatMode;
        mRepeatCount = repeatCount;

        boolean none = repeatMode == Reminder.REPEAT_MODE_NONE;
        ViewUtil.setVisible(none, mTextRepeat);
        ViewUtil.setVisible(!none, mEditRepeatLayout);
        if (none) {
            ViewUtil.setText(mTextRepeat, R.string.repeat_mode_none);
        } else {
            mEditRepeatCount.setText(String.valueOf(repeatCount));
            mTextRepeatPeriod.setText(getResources().getStringArray(R.array.repeat_periods)[repeatMode]);
        }

    }

    private void showDatePicker() {
        if (mDateTime != null) {
            int year = mDateTime.getYear();
            int month = mDateTime.getMonthOfYear()-1;
            int day = mDateTime.getDayOfMonth();

            DatePickerDialog dialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
                mDateTime.setYear(year1);
                mDateTime.setMonthOfYear(monthOfYear+1);
                mDateTime.setDayOfMonth(dayOfMonth);

                ViewUtil.setText(mTextDate, DateFormatter.formatDate(getApplication(), mDateTime.getMillis(), true));
            }, year, month, day);
            dialog.show();
        }
    }

    private void showTimePicker() {
        if (mDateTime != null) {
            final boolean is24H = DateFormat.is24HourFormat(this);

            int hour = mDateTime.getHourOfDay();
            int minute = mDateTime.getMinuteOfHour();

            TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
                mDateTime.setHourOfDay(hourOfDay);
                mDateTime.setMinuteOfHour(minute1);

                ViewUtil.setText(mTextTime, DateFormatter.formatTime(getApplication(), mDateTime.getMillis()));
            }, hour, minute, is24H);
            dialog.show();
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkNotificationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_reminder, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        MenuUtil.show(menu, mReminder != null, R.id.menu_delete);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_done) {
            applyReminder();
            return true;
        } else if (itemId == R.id.menu_delete) {
            deleteReminder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteReminder() {
        if (mReminder != null) {
            mDeleteReminderInteractor.execute(mReminder.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleCompletableObserver() {
                    @Override
                    public void onComplete() {
                        mReminderScheduler.cancel(mNoteId);
                        EventBusHelper.deleteReminder();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (mToolbar != null) {
                            Snackbar.make(mToolbar, R.string.error, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    private void applyReminder() {
        Completable completable;
        if (mDateTime != null && notEmpty(mNoteId)) {
            mDateTime.setSecondOfMinute(0);
            mDateTime.setMillisOfSecond(0);

            if (mReminder == null) {
                mReminder = new Reminder();
                mReminder.setNoteId(mNoteId);
                completable = mInsertReminderInteractor.execute(mReminder, mNoteId);
            } else {
                mReminderScheduler.cancel(mNoteId);
                completable = mUpdateReminderInteractor.execute(mReminder);
            }

            mReminder.setDateTime(mDateTime.toDateTime());
            if (mRepeatCount == 0) {
                mRepeatMode = Reminder.REPEAT_MODE_NONE;
                mRepeatCount = 1;
            }
            if (mRepeatCount < 0) {
                mRepeatCount *= -1;
            }
            mReminder.setRepeatMode(mRepeatMode);
            mReminder.setRepeatCount(mRepeatCount);

            completable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleCompletableObserver() {
                    @Override
                    public void onComplete() {
                        if (notEmpty(mNoteId) && mDateTime != null) {
                            EventBusHelper.applyReminder();
                            mReminderScheduler.schedule(mReminder);
                        }
                        finish();
                    }
                });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDateTime != null) {
            outState.putInt(EXTRA_YEAR, mDateTime.getYear());
            outState.putInt(EXTRA_MONTH, mDateTime.getMonthOfYear());
            outState.putInt(EXTRA_DAY, mDateTime.getDayOfMonth());
            outState.putInt(EXTRA_HOUR, mDateTime.getHourOfDay());
            outState.putInt(EXTRA_MINUTE, mDateTime.getMinuteOfHour());
        }
        outState.putInt(EXTRA_REPEAT_MODE, mRepeatMode);
        outState.putInt(EXTRA_REPEAT_COUNT, mRepeatCount);
    }
}

package com.axel_stein.noteapp.notes.edit.check_list;

import android.widget.Checkable;

public class CheckItem implements Checkable {

    private boolean mChecked;

    private String mTitle;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public void setChecked(boolean b) {
        mChecked = b;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        mChecked = !mChecked;
    }

}

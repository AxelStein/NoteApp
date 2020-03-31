package com.axel_stein.noteapp.main.edit;

public class DataCheckItem extends CheckItem {
    private long views;

    public DataCheckItem(String text, long views) {
        super(text);
        this.views = views;
        setCheckable(false);
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }
}

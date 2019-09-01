package com.axel_stein.noteapp.dialogs.main_menu;

public class PrimaryItem implements Item {
    private String id;
    private String title;
    private int titleRes;
    private boolean selectable = true;
    private int icon;

    public PrimaryItem fromId(String id) {
        this.id = id;
        return this;
    }

    public PrimaryItem fromTitle(String title) {
        this.title = title;
        return this;
    }

    public PrimaryItem fromTitle(int titleRes) {
        this.titleRes = titleRes;
        return this;
    }

    public PrimaryItem fromCheckable(boolean checkable) {
        this.selectable = checkable;
        return this;
    }

    public PrimaryItem fromIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean hasTitleRes() {
        return titleRes > 0;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public void setTitleRes(int titleRes) {
        this.titleRes = titleRes;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}

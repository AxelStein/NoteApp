package com.axel_stein.noteapp.main.edit;

class CheckItem {
    private String text;
    private boolean checked;
    private boolean checkable = true;

    CheckItem() {
    }

    CheckItem(String text) {
        this.text = text;
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    boolean isChecked() {
        return checked;
    }

    void setChecked(boolean checked) {
        this.checked = checked;
    }

    boolean isCheckable() {
        return checkable;
    }

    void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    @Override
    public String toString() {
        return "CheckItem{" +
                "text='" + text + '\'' +
                ", checked=" + checked +
                ", checkable=" + checkable +
                '}';
    }
}

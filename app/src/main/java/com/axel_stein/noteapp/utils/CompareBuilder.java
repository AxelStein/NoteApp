package com.axel_stein.noteapp.utils;

public class CompareBuilder {
    private boolean mResultFalse;

    public CompareBuilder append(boolean a) {
        add(a);
        return this;
    }

    public CompareBuilder append(int a, int b) {
        add(a == b);
        return this;
    }

    public CompareBuilder append(long a, long b) {
        add(a == b);
        return this;
    }

    public CompareBuilder append(float a, float b) {
        add(a == b);
        return this;
    }

    public CompareBuilder append(boolean a, boolean b) {
        add(a == b);
        return this;
    }

    public CompareBuilder append(String a, String b) {
        add((a == null && b == null) || isEmpty(a) && isEmpty(b) || (a != null && b != null) && a.contentEquals(b));
        return this;
    }

    public CompareBuilder append(Object a, Object b) {
        add((a == null && b == null) || (a != null && b != null) && a.equals(b));
        return this;
    }

    private void add(boolean compare) {
        if (!compare) {
            mResultFalse = true;
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public boolean areEqual() {
        boolean equal = !mResultFalse;
        mResultFalse = false;
        return equal;
    }

}

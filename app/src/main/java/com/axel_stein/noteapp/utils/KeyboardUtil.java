package com.axel_stein.noteapp.utils;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {

    private KeyboardUtil() {
    }

    public static void show(@Nullable View v) {
        if (v != null) {
            v.post(new KeyboardRunnable(v));
        }
    }

    public static void hide(@Nullable View v) {
        if (v != null) {
            InputMethodManager imm = getInputManager(v.getContext());
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            v.clearFocus();
        }
    }

    public static void hide(Activity activity) {
        InputMethodManager imm = getInputManager(activity);
        if (imm != null) {
            imm.hideSoftInputFromWindow(activity.findViewById(android.R.id.content).getWindowToken(), 0);
        }
    }

    @Nullable
    private static InputMethodManager getInputManager(@Nullable Context context) {
        return context == null ? null : (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private static class KeyboardRunnable implements Runnable {
        private static final String TAG = "KeyboardRunnable";

        private static final int INTERVAL_MS = 100;

        @Nullable
        private View mView = null;

        KeyboardRunnable(@Nullable View view) {
            this.mView = view;
        }

        @Override
        public void run() {
            if (mView == null) {
                Log.e(TAG, "Invalid Params");
                return;
            }

            InputMethodManager imm = (InputMethodManager) mView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!(mView.isFocusable() && mView.isFocusableInTouchMode())) { // Check view is focusable
                Log.e(TAG, "Non focusable view");
            } else if (!mView.requestFocus()) { // Try focusing
                Log.e(TAG, "Cannot focus on view");
                post();
            } else if (imm != null && !imm.isActive(mView)) { // Check if Imm is active with this view
                Log.e(TAG, "IMM is not active");
                post();
            } else if (imm != null && !imm.showSoftInput(mView, InputMethodManager.SHOW_IMPLICIT)) { // Show Keyboard
                Log.e(TAG, "Unable to show keyboard");
                post();
            }
        }

        private void post() {
            if (mView != null) {
                mView.postDelayed(this, INTERVAL_MS);
            }
        }

    }

}


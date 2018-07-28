package com.axel_stein.noteapp.main;

import android.view.View;
import android.widget.TextView;

import com.axel_stein.noteapp.views.IconTextView;

public interface SortPanelListener {

    View getSortPanel();

    TextView getCounter();

    IconTextView getSortTitle();

}

package com.axel_stein.noteapp.dialogs.main_menu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.SimpleSingleObserver;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;
import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class MainMenuDialog extends BottomSheetDialogFragment {
    public static final String ID_INBOX = "ID_INBOX";
    public static final String ID_STARRED = "ID_STARRED";
    public static final String ID_REMINDERS = "ID_REMINDERS";
    public static final String ID_TRASH = "ID_TRASH";
    public static final String ID_ADD_NOTEBOOK = "ID_ADD_NOTEBOOK";
    private static final String BUNDLE_SELECTED_ITEM_ID = "BUNDLE_SELECTED_ITEM_ID";

    public static void launch(FragmentManager manager, String tag, String selectedItemId) {
        MainMenuDialog dialog = new MainMenuDialog();
        dialog.mSelectedItemId = selectedItemId;
        dialog.show(manager, tag);
    }

    private TextView mTextUserName;
    private TextView mTextUserEmail;
    private CircleImageView mImageViewUserPhoto;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private OnMenuItemClickListener mListener;
    private String mSelectedItemId;

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment fragment = getTargetFragment();
        if (fragment != null) {
            setListeners(fragment);
        } else {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                setListeners(activity);
            }
        }
    }

    private void setListeners(Object o) {
        if (o instanceof OnMenuItemClickListener) {
            mListener = (OnMenuItemClickListener) o;
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        mListener = null;
        mTextUserName = null;
        mTextUserEmail = null;
        mImageViewUserPhoto = null;
        mRecyclerView = null;
        mAdapter = null;
        super.onDestroyView();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View view = inflater.inflate(R.layout.dialog_main_menu, null);
        view.findViewById(R.id.user_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onUserPanelClick(MainMenuDialog.this);
                }
            }
        });
        view.findViewById(R.id.button_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSettingsClick(MainMenuDialog.this);
                }
            }
        });

        mTextUserName = view.findViewById(R.id.text_user_name);
        mTextUserEmail = view.findViewById(R.id.text_user_email);
        mImageViewUserPhoto = view.findViewById(R.id.user_photo);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        if (savedInstanceState != null) {
            mSelectedItemId = savedInstanceState.getString(BUNDLE_SELECTED_ITEM_ID);
        }
        loadNotebooks(getActivity(), mSelectedItemId);

        checkNotNull(getContext());
        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme());
        dialog.setContentView(view);
        return dialog;
    }

    private void loadNotebooks(final Context context, final String selectedItemId) {
        mQueryNotebookInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleSingleObserver<List<Notebook>>() {
                    @Override
                    public void onSuccess(List<Notebook> notebooks) {
                        List<Item> items = new ArrayList<>();
                        items.add(new PrimaryItem().fromId(ID_INBOX)
                                .fromTitle(R.string.action_inbox)
                                .fromIcon(R.drawable.ic_inbox_24dp));
                        items.add(new PrimaryItem()
                                .fromId(ID_STARRED)
                                .fromTitle(R.string.action_starred)
                                .fromIcon(R.drawable.ic_star_border_24dp));
                        items.add(new PrimaryItem()
                                .fromId(ID_REMINDERS)
                                .fromTitle(R.string.action_reminders)
                                .fromIcon(R.drawable.ic_notifications_none_24dp));
                        for (Notebook notebook : notebooks) {
                            items.add(new PrimaryItem()
                                    .fromId(notebook.getId())
                                    .fromTitle(notebook.getTitle())
                                    .fromIcon(R.drawable.ic_book_24dp));
                        }
                        items.add(new PrimaryItem()
                                .fromId(ID_ADD_NOTEBOOK)
                                .fromTitle(R.string.action_add_notebook)
                                .fromIcon(R.drawable.ic_add_box_24dp)
                                .fromCheckable(false));
                        items.add(new DividerItem());

                        items.add(new PrimaryItem()
                                .fromId(ID_TRASH)
                                .fromTitle(R.string.action_trash)
                                .fromIcon(R.drawable.ic_delete_24dp));

                        setItems(items, selectedItemId);

                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
                        if (account != null) {
                            setUserName(account.getDisplayName());
                            setUserEmail(account.getEmail());
                            setUserPhotoUrl(account.getPhotoUrl());
                        } else {
                            setUserName(getString(R.string.action_sign_in));
                            setUserEmail(null);
                            setUserPhotoUrl(null);
                        }
                    }
                });
    }

    private void setItems(List<Item> items, String selectedItemId) {
        mAdapter = new Adapter(getContext());
        mAdapter.setItems(items);
        mAdapter.setSelectedItemId(selectedItemId);
        mAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(PrimaryItem item) {
                if (mListener != null) {
                    mListener.onMenuItemClick(MainMenuDialog.this, item);
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setUserName(String name) {
        ViewUtil.setText(mTextUserName, name);
        ViewUtil.setVisible(notEmpty(name), mTextUserName);
    }

    private void setUserEmail(String email) {
        ViewUtil.setText(mTextUserEmail, email);
        ViewUtil.setVisible(notEmpty(email), mTextUserEmail);
    }

    private void setUserPhotoUrl(Uri url) {
        Picasso.get().load(url).placeholder(R.drawable.ic_account_circle_36dp).into(mImageViewUserPhoto);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_SELECTED_ITEM_ID, mSelectedItemId);
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(MainMenuDialog dialog, PrimaryItem item);
        void onUserPanelClick(MainMenuDialog dialog);
        void onSettingsClick(MainMenuDialog dialog);
    }

}

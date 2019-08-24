package com.axel_stein.noteapp.dialogs.main_menu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.settings.SettingsActivity;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainMenuDialog extends BottomSheetDialogFragment {
    private List<Item> mItems;
    private String mSelectedItemId;
    private OnMenuItemClickListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
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
        View mTextSignIn = view.findViewById(R.id.text_sign_in);
        TextView mTextUserName = view.findViewById(R.id.text_user_name);
        TextView mTextUserEmail = view.findViewById(R.id.text_user_email);
        CircleImageView mUserPhoto = view.findViewById(R.id.user_photo);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null) {
            //mDriveResourceClient = Drive.getDriveResourceClient(mContext, account);
            ViewUtil.show(mTextUserName, mTextUserEmail);
            ViewUtil.hide(mTextSignIn);

            mTextUserName.setText(account.getDisplayName());
            mTextUserEmail.setText(account.getEmail());

            Picasso.get().load(account.getPhotoUrl()).placeholder(R.drawable.ic_account_circle_36).into(mUserPhoto);
        } else {
            ViewUtil.hide(mTextUserName, mTextUserEmail);
            ViewUtil.show(mTextSignIn);
        }


        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.button_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
            }
        });

        Adapter adapter = new Adapter(getContext());
        adapter.setItems(mItems);
        adapter.setSelectedItemId(mSelectedItemId);
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(PrimaryItem item) {
                if (mListener != null) {
                    mListener.onMenuItemClick(MainMenuDialog.this, item);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme());
        dialog.setContentView(view);
        return dialog;
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(MainMenuDialog dialog, PrimaryItem item);
        void onUserPanelClick(MainMenuDialog dialog);
    }

    public static class Builder {
        private String mSelectedItemId;
        private List<Item> mItems;

        public Builder setSelectedItemId(String itemId) {
            mSelectedItemId = itemId;
            return this;
        }

        public Builder addItem(Item item) {
            if (mItems == null) {
                mItems = new ArrayList<>();
            }
            mItems.add(item);
            return this;
        }

        public void show(FragmentActivity activity, String tag) {
            show(activity.getSupportFragmentManager(), tag);
        }

        public void show(Fragment fragment, String tag) {
            show(fragment.getFragmentManager(), tag);
        }

        public void show(FragmentManager manager, String tag) {
            MainMenuDialog dialog = new MainMenuDialog();
            dialog.mItems = mItems;
            dialog.mSelectedItemId = mSelectedItemId;

            dialog.show(manager, tag);
        }

    }

}

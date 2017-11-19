package com.axel_stein.noteapp.backup;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.axel_stein.domain.model.BackupFile;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.backup.BackupContract.Presenter;
import com.axel_stein.noteapp.dialogs.LoadingDialog;
import com.axel_stein.noteapp.dialogs.backup.RenameBackupFileDialog;
import com.axel_stein.noteapp.utils.ViewUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class BackupFragment extends Fragment implements BackupContract.View {

    private static final int REQUEST_WRITE_STORAGE = 111;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_view)
    TextView mEmptyView;

    @Nullable
    private Presenter mPresenter;

    @Nullable
    private Adapter mAdapter;

    private LoadingDialog mImportDialog;

    private BackupItemListener mItemListener = new BackupItemListener() {
        @Override
        public void onItemClick(int pos, BackupFile file) {
            mPresenter.onFileClick(file);
        }

        @Override
        public void onMenuClick(int pos, BackupFile file, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_import:
                    mPresenter.importFile(file);
                    break;

                case R.id.menu_send:
                    sendFile(file);
                    break;

                case R.id.menu_rename:
                    //mPresenter.renameFile(file, null);
                    RenameBackupFileDialog.create(file).show(getFragmentManager(), null);
                    break;

                case R.id.menu_delete:
                    mPresenter.deleteFile(file);
                    break;

            }
        }
    };

    private void sendFile(BackupFile backup) {
        File file = backup.getFile();
        if (file == null) {
            Log.e("TAG", "file is null");
            // todo file not found message
            EventBusHelper.showMessage(R.string.error);
            return;
        }

        Uri fileUri = FileProvider.getUriForFile(getContext(), "com.axel_stein.noteapp.fileprovider", file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setFlags(FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_READ_URI_PERMISSION);

        // Workaround for Android bug.
        // grantUriPermission also needed for KITKAT,
        // see https://code.google.com/p/android/issues/detail?id=76683
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<ResolveInfo> resInfoList = getContext().getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, fileUri, FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("TAG", "send: no activity found");
            EventBusHelper.showMessage(R.string.error_share);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mPresenter = new BackupPresenter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                mPresenter.addFile();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_backup, container, false);
        ButterKnife.bind(this, root);

        mAdapter = new Adapter();
        mAdapter.setListener(mItemListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(divider);

        if (mPresenter != null) {
            mPresenter.onCreateView(this);
        }

        requestWriteStoragePermission();

        return root;
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        mRecyclerView = null;
        mEmptyView = null;
        if (mPresenter != null) {
            mPresenter.onDestroyView();
        }
        super.onDestroyView();
    }

    @Override
    public void setItems(List<BackupFile> items) {
        if (mAdapter != null) {
            mAdapter.setItems(items);
        }
        ViewUtil.show(items != null && items.size() == 0, mEmptyView);
    }

    @Override
    public void showImportDialog() {
        if (mImportDialog == null) {
            mImportDialog = LoadingDialog.from(R.string.title_import, R.string.msg_wait);
            mImportDialog.show(getFragmentManager());
        }
    }

    @Override
    public void dismissImportDialog() {
        if (mImportDialog != null) {
            mImportDialog.dismiss();
        }
        mImportDialog = null;
    }

    private void requestWriteStoragePermission() {
        //ask for the permission in android M
        int permission = ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE);
        if (permission != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Permission to access the SD-Card is required for this app to create backup.");
                builder.setTitle("Permission required");
                builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        makeRequest();
                    }
                });
                builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                makeRequest();
            }
        }
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length == 0 || grantResults[0] != PERMISSION_GRANTED) {
                    Log.i(getClass().getSimpleName(), "Permission has been denied by user");
                } else {
                    Log.i(getClass().getSimpleName(), "Permission has been granted by user");
                }
                break;
            }
        }
    }

    private interface BackupItemListener {

        void onItemClick(int pos, BackupFile file);

        void onMenuClick(int pos, BackupFile file, MenuItem item);

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private List<BackupFile> mItems;
        private BackupItemListener mListener;

        void setItems(List<BackupFile> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        void setListener(BackupItemListener listener) {
            mListener = listener;
        }

        private BackupFile getItem(int position) {
            if (position >= 0 && position < getItemCount()) {
                return mItems.get(position);
            }
            return null;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_backup, parent, false);
            return new ViewHolder(view, mListener);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BackupFile file = getItem(position);
            if (file != null) {
                ViewUtil.setText(holder.mName, file.getName());
                ViewUtil.setText(holder.mInfo, file.getInfo());
            }
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView mName;

            TextView mInfo;

            ImageButton mMenu;

            ViewHolder(View itemView, final BackupItemListener listener) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            int pos = getAdapterPosition();
                            if (pos >= 0 && pos < getItemCount()) {
                                listener.onItemClick(pos, getItem(pos));
                            }
                        }
                    }
                });
                mName = itemView.findViewById(R.id.text_name);
                mInfo = itemView.findViewById(R.id.text_info);
                mMenu = itemView.findViewById(R.id.button_menu);
                mMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu menu = new PopupMenu(getContext(), v, Gravity.TOP | Gravity.END, 0, R.style.PopupMenu);
                        menu.inflate(R.menu.item_backup);
                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (listener != null) {
                                    int pos = getAdapterPosition();
                                    if (pos >= 0 && pos < getItemCount()) {
                                        listener.onMenuClick(pos, getItem(pos), item);
                                    }
                                }
                                return true;
                            }
                        });
                        menu.show();
                    }
                });
            }
        }

    }

}

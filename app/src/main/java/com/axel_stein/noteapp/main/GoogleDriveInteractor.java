package com.axel_stein.noteapp.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.noteapp.App;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class GoogleDriveInteractor implements DriveSyncRepository {
    private static final String FILE_NOTEBOOKS_TITLE = "notebooks.json";
    private static final String FILE_LABELS_TITLE = "labels.json";
    private static final String FILE_NOTE_LABEL_PAIRS_TITLE = "note_label_pairs.json";
    private static final String FILE_SETTINGS_TITLE = "settings.json";

    public static class UserData {
        private Uri photo;
        private String name;

        UserData(Uri photo, String name) {
            this.photo = photo;
            this.name = name;
        }

        public Uri getPhoto() {
            return photo;
        }

        public String getName() {
            return name;
        }
    }

    private interface OnLoadListener {
        void onLoad(@Nullable DriveFile file);
    }

    public interface OnSignInListener {
        void onSuccess(UserData user);
    }

    public interface OnSignOutListener {
        void onSuccess();
    }

    private App mContext;
    private DriveResourceClient mDriveResourceClient;
    private UserData mUserData;

    public GoogleDriveInteractor(App app) {
        mContext = app;
        signIn(null);
    }

    @NonNull
    public Intent getSignInIntent() {
        return buildGoogleSignInClient().getSignInIntent();
    }

    public void signIn(OnSignInListener listener) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);
        if (account != null) {
            mDriveResourceClient = Drive.getDriveResourceClient(mContext, account);
            mUserData = new UserData(account.getPhotoUrl(), account.getDisplayName());
            if (listener != null) {
                listener.onSuccess(mUserData);
            }
        }
    }

    public boolean isSignedIn() {
        return mDriveResourceClient != null;
    }

    public void signOut(final OnSignOutListener listener) {
        GoogleSignInClient client = buildGoogleSignInClient();
        client.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mUserData = null;
                mDriveResourceClient = null;
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(mContext, signInOptions);
    }

    @Nullable
    public UserData getUserData() {
        if (isSignedIn()) {
            return mUserData;
        }
        return null;
    }

    @SuppressLint("CheckResult")
    @Override
    public void notifyNotebooksChanged(final List<Notebook> notebooks) {
        if (!isSignedIn()) {
            return;
        }

        Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                String data = mapper.writeValueAsString(notebooks);
                Log.w("TAG", "notifyNotebooksChanged = " + data);
                return data;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                loadNotebooks(s);
            }
        });
    }

    private void loadNotebooks(final String data) {
        loadFile(FILE_NOTEBOOKS_TITLE, new OnLoadListener() {
            @Override
            public void onLoad(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(FILE_NOTEBOOKS_TITLE, data);
                } else {
                    updateFile(file, data);
                }
            }
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public void notifyLabelsChanged(final List<Label> labels) {
        if (!isSignedIn()) {
            return;
        }

        Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                String data = mapper.writeValueAsString(labels);
                Log.w("TAG", "notifyLabelsChanged = " + data);
                return data;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                loadLabels(s);
            }
        });
    }

    private void loadLabels(final String data) {
        loadFile(FILE_LABELS_TITLE, new OnLoadListener() {
            @Override
            public void onLoad(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(FILE_LABELS_TITLE, data);
                } else {
                    updateFile(file, data);
                }
            }
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public void notifyNoteLabelPairsChanged(final List<NoteLabelPair> pairs) {
        if (!isSignedIn()) {
            return;
        }

        Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                String data = mapper.writeValueAsString(pairs);
                Log.w("TAG", "notifyNoteLabelPairsChanged = " + data);
                return data;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                loadNoteLabelPairs(s);
            }
        });
    }

    private void loadNoteLabelPairs(final String data) {
        loadFile(FILE_NOTE_LABEL_PAIRS_TITLE, new OnLoadListener() {
            @Override
            public void onLoad(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(FILE_NOTE_LABEL_PAIRS_TITLE, data);
                } else {
                    updateFile(file, data);
                }
            }
        });
    }

    @Override
    public void notifySettingsChanged(final String data) {
        if (!isSignedIn()) {
            return;
        }

        Log.w("TAG", "notifySettingsChanged = " + data);

        loadFile(FILE_SETTINGS_TITLE, new OnLoadListener() {
            @Override
            public void onLoad(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(FILE_SETTINGS_TITLE, data);
                } else {
                    updateFile(file, data);
                }
            }
        });
    }

    private void loadFile(@NonNull final String title, @NonNull final OnLoadListener l) {
        Log.d("TAG", "loadFile = " + title);

        mDriveResourceClient.getAppFolder().addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {
                Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, title)).build();
                mDriveResourceClient.queryChildren(driveFolder, query).addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer buffer) {
                        if (buffer.getCount() == 0) {
                            l.onLoad(null);
                        } else {
                            Metadata metadata = buffer.get(0);
                            l.onLoad(metadata.getDriveId().asDriveFile());
                        }
                    }
                });
            }
        });
    }

    private void createFile(final String title, final String data) {
        Log.d("TAG", "createFile = " + title);

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        Tasks.whenAll(appFolderTask, createContentsTask).continueWithTask(new Continuation<Void, Task<DriveFile>>() {
            @Override
            public Task<DriveFile> then(@android.support.annotation.NonNull Task<Void> task) throws Exception {
                DriveFolder appFolder = appFolderTask.getResult();
                DriveContents contents = createContentsTask.getResult();

                Writer writer = new OutputStreamWriter(contents.getOutputStream());
                writer.write(data);

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(title)
                        .setMimeType("text/plain")
                        .build();

                return mDriveResourceClient.createFile(appFolder, changeSet, contents);
            }
        }).addOnSuccessListener(new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile file) {
                Log.d("TAG", "created " + file.getDriveId().encodeToString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "Unable to create file", e);
            }
        });
    }

    private void updateFile(final DriveFile file, final String data) {
        Log.d("TAG", "updateFile = " + file.getDriveId().encodeToString());

        Task<DriveContents> openTask = mDriveResourceClient.openFile(file, DriveFile.MODE_WRITE_ONLY);
        openTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                DriveContents contents = task.getResult();

                Writer writer = new OutputStreamWriter(contents.getOutputStream());
                writer.write(data);

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setLastViewedByMeDate(new Date())
                        .build();

                return mDriveResourceClient.commitContents(contents, changeSet);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "updated " + file.getDriveId().encodeToString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "Unable to update file", e);
            }
        });
    }

}

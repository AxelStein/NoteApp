package com.axel_stein.noteapp.google_drive;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.model.JsonNoteWrapper;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.utils.validators.LabelValidator;
import com.axel_stein.domain.utils.validators.NoteLabelPairValidator;
import com.axel_stein.domain.utils.validators.NoteValidator;
import com.axel_stein.domain.utils.validators.NotebookValidator;
import com.axel_stein.noteapp.App;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.noteapp.google_drive.FileHelper.createFile;
import static com.axel_stein.noteapp.google_drive.FileHelper.deleteFile;
import static com.axel_stein.noteapp.google_drive.FileHelper.downloadFile;
import static com.axel_stein.noteapp.google_drive.FileHelper.updateFile;
import static com.axel_stein.noteapp.google_drive.LogHelper.logWarning;

public class GoogleDriveInteractor implements DriveSyncRepository {
    private static final String FILE_NOTEBOOKS_TITLE = "notebooks.json";
    private static final String FILE_LABELS_TITLE = "labels.json";
    private static final String FILE_NOTE_LABEL_PAIRS_TITLE = "note_label_pairs.json";
    private static final String FILE_SETTINGS_TITLE = "settings.json";

    private App mContext;
    private DriveResourceClient mDriveResourceClient;
    private UserData mUserData;

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NotebookRepository mNotebookRepository;

    @NonNull
    private LabelRepository mLabelRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    @NonNull
    private AppSettingsRepository mSettingsRepository;

    public GoogleDriveInteractor(@NonNull App app,
                                 @NonNull NoteRepository n,
                                 @NonNull NotebookRepository b,
                                 @NonNull LabelRepository l,
                                 @NonNull NoteLabelPairRepository p,
                                 @NonNull AppSettingsRepository s) {
        mContext = app;
        mNoteRepository = requireNonNull(n);
        mNotebookRepository = requireNonNull(b);
        mLabelRepository = requireNonNull(l);
        mNoteLabelPairRepository = requireNonNull(p);
        mSettingsRepository = requireNonNull(s);
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
            mUserData = new UserData(account.getPhotoUrl(), account.getDisplayName(), account.getEmail());
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
                        .requestEmail()
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
    public void notifyNoteChanged(final Note note) {
        Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                String data = mapper.writeValueAsString(new JsonNoteWrapper(note));
                logWarning("notifyNoteChanged", data);
                return data;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                uploadNote(note.getId(), s);
            }
        });
    }

    private void uploadNote(final String title, final String data) {
        downloadFile(mDriveResourceClient, title, new OnDownloadListener() {
            @Override
            public void onDownload(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(mDriveResourceClient, title, data, true);
                } else {
                    updateFile(mDriveResourceClient, file, data, true);
                }
            }
        });
    }

    @Override
    public void notifyNoteDeleted(Note note) {
        downloadFile(mDriveResourceClient, note.getId(), new OnDownloadListener() {
            @Override
            public void onDownload(@Nullable DriveFile file) {
                if (file != null) {
                    deleteFile(mDriveResourceClient, file);
                }
            }
        });
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
                logWarning("notifyNotebooksChanged", data);
                return data;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                uploadNotebooks(s);
            }
        });
    }

    private void uploadNotebooks(final String data) {
        downloadFile(mDriveResourceClient, FILE_NOTEBOOKS_TITLE, new OnDownloadListener() {
            @Override
            public void onDownload(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(mDriveResourceClient, FILE_NOTEBOOKS_TITLE, data);
                } else {
                    updateFile(mDriveResourceClient, file, data);
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
                logWarning("notifyLabelsChanged", data);
                return data;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                uploadLabels(s);
            }
        });
    }

    private void uploadLabels(final String data) {
        downloadFile(mDriveResourceClient, FILE_LABELS_TITLE, new OnDownloadListener() {
            @Override
            public void onDownload(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(mDriveResourceClient, FILE_LABELS_TITLE, data);
                } else {
                    updateFile(mDriveResourceClient, file, data);
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
                logWarning("notifyNoteLabelPairsChanged", data);
                return data;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                uploadNoteLabelPairs(s);
            }
        });
    }

    private void uploadNoteLabelPairs(final String data) {
        downloadFile(mDriveResourceClient, FILE_NOTE_LABEL_PAIRS_TITLE, new OnDownloadListener() {
            @Override
            public void onDownload(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(mDriveResourceClient, FILE_NOTE_LABEL_PAIRS_TITLE, data);
                } else {
                    updateFile(mDriveResourceClient, file, data);
                }
            }
        });
    }

    @Override
    public void notifySettingsChanged(final String data) {
        if (!isSignedIn()) {
            return;
        }

        logWarning("notifySettingsChanged", data);

        downloadFile(mDriveResourceClient, FILE_SETTINGS_TITLE, new OnDownloadListener() {
            @Override
            public void onDownload(@Nullable DriveFile file) {
                if (file == null) {
                    createFile(mDriveResourceClient, FILE_SETTINGS_TITLE, data);
                } else {
                    updateFile(mDriveResourceClient, file, data);
                }
            }
        });
    }

    @SuppressLint("CheckResult")
    public void importAndReplace() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                logWarning("importAndReplace");

                mNoteRepository.deleteAll();
                mNotebookRepository.deleteAll();
                mLabelRepository.deleteAll();
                mNoteLabelPairRepository.deleteAll();

                ObjectMapper mapper = new ObjectMapper();

                String jsonNotebooks = FileHelper.readFileSync(mDriveResourceClient, FILE_NOTEBOOKS_TITLE);
                ArrayList<Notebook> notebooks = mapper.readValue(jsonNotebooks, new TypeReference<ArrayList<Notebook>>(){});
                if (notebooks != null) {
                    for (Notebook notebook : notebooks) {
                        if (!NotebookValidator.isValid(notebook)) {
                            System.out.println("Error: notebook is not valid = " + notebook);
                        } else {
                            mNotebookRepository.insert(notebook);
                        }
                    }
                } else {
                    System.out.println("Error: notebooks not found");
                }

                String jsonLabels = FileHelper.readFileSync(mDriveResourceClient, FILE_LABELS_TITLE);
                ArrayList<Label> labels = mapper.readValue(jsonLabels, new TypeReference<ArrayList<Label>>(){});
                if (labels != null) {
                    for (Label label : labels) {
                        if (!LabelValidator.isValid(label)) {
                            System.out.println("Error: label is not valid = " + label);
                        } else {
                            mLabelRepository.insert(label);
                        }
                    }
                } else {
                    System.out.println("Error: labels not found");
                }

                String jsonSettings = FileHelper.readFileSync(mDriveResourceClient, FILE_SETTINGS_TITLE);
                mSettingsRepository.importSettings(jsonSettings);

                String jsonPairs = FileHelper.readFileSync(mDriveResourceClient, FILE_NOTE_LABEL_PAIRS_TITLE);
                ArrayList<NoteLabelPair> pairs = mapper.readValue(jsonPairs, new TypeReference<ArrayList<NoteLabelPair>>(){});
                if (pairs != null) {
                    for (NoteLabelPair pair : pairs) {
                        if (!NoteLabelPairValidator.isValid(pair)) {
                            System.out.println("Error: pair is not valid = " + pair);
                        } else {
                            mNoteLabelPairRepository.insert(pair);
                        }
                    }
                } else {
                    System.out.println("Error: pairs not found");
                }

                List<String> jsonNotes = FileHelper.downloadStarredFilesSync(mDriveResourceClient);
                for (String data : jsonNotes) {
                    JsonNoteWrapper wrapper = mapper.readValue(data, JsonNoteWrapper.class);
                    Note note = wrapper.toNote();
                    logWarning(String.valueOf(note));
                    if (!NoteValidator.isValid(note)) {
                        System.out.println("Error: note is not valid = " + note);
                    } else {
                        mNoteRepository.insert(note);
                    }
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                logWarning("Import completed");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        });
    }

}

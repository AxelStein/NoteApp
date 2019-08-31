package com.axel_stein.noteapp.google_drive;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.FileUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.axel_stein.data.AppSettingsRepository.BACKUP_FILE_NAME;
import static com.axel_stein.domain.utils.TextUtil.isEmpty;
import static com.axel_stein.domain.utils.TextUtil.notEmpty;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private Context mContext;
    private Drive mDriveService;
    private AppSettingsRepository mSettings;

    public DriveServiceHelper(Context context, AppSettingsRepository settings) {
        mContext = context;
        mSettings = settings;
        setupDriveService();
    }

    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(mContext) != null;
    }

    public Intent requestSignInIntent() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA), new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(mContext, signInOptions);
        // The result of the sign-in Intent is handled in onActivityResult.
        return client.getSignInIntent();
    }

    public void signOut(OnSuccessListener<Void> l) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA), new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(mContext, signInOptions);
        client.signOut().addOnSuccessListener(l).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mSettings.removeBackupFileDriveId();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean setupDriveService() {
        if (mDriveService == null) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);
            if (account != null) {
                ArrayList<String> list = new ArrayList<>();
                list.add(DriveScopes.DRIVE_APPDATA);
                list.add(DriveScopes.DRIVE_FILE);

                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, list);
                credential.setSelectedAccount(account.getAccount());
                mDriveService = new Drive.Builder(new NetHttpTransport(),
                        new GsonFactory(), credential)
                        .setApplicationName(mContext.getString(R.string.app_name))
                        .build();
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    // ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", data);
    public void uploadBackup(final java.io.File file, OnSuccessListener<String> l) {
        Tasks.call(Executors.newSingleThreadExecutor(), new Callable<String>() {
            @Override
            public String call() throws Exception {
                uploadBackupSync(file);
                return mSettings.getBackupFileDriveId();
            }
        }).addOnSuccessListener(l).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void uploadBackupSync(final java.io.File file) throws IOException {
        Log.e("TAG", "upload " + file.getName());

        File metadata = new File().setMimeType("text/plain").setName(file.getName());
        FileContent content = new FileContent("text/plain", file);
        File uploadedFile;
        if (setupDriveService()) {
            String id = mSettings.getBackupFileDriveId();
            if (isEmpty(id)) {
                id = fetchBackupFileId();
            }
            if (isEmpty(id)) {
                metadata.setParents(Collections.singletonList("appDataFolder"));
                uploadedFile = mDriveService.files().create(metadata, content).setFields("id").execute();
            } else {
                uploadedFile = mDriveService.files().update(id, metadata, content).execute();
            }

            mSettings.storeBackupFileDriveId(uploadedFile.getId());

            Log.e("TAG", "file uploaded " + id);
        }
    }

    public void getModifiedDate(OnSuccessListener<Long> l) {
        Tasks.call(Executors.newSingleThreadExecutor(), new Callable<Long>() {
            @Override
            public Long call() {
                return getModifiedDateSync();
            }
        }).addOnSuccessListener(l).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public long getModifiedDateSync() {
        String id = mSettings.getBackupFileDriveId();
        if (isEmpty(id)) {
            id = fetchBackupFileId();
        }
        if (notEmpty(id) && setupDriveService()) {
            try {
                File f = mDriveService.files().get(id).setFields("modifiedTime").execute();
                return f.getModifiedTime().getValue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public void downloadBackup(OnSuccessListener<String> l) {
        Tasks.call(Executors.newSingleThreadExecutor(), new Callable<String>() {
            @Override
            public String call() throws Exception {
                String id = mSettings.getBackupFileDriveId();
                if (isEmpty(id)) {
                    id = fetchBackupFileId();
                }
                if (notEmpty(id) && setupDriveService()) {
                    java.io.File dir = mContext.getFilesDir();
                    java.io.File localBackup = new java.io.File(dir, BACKUP_FILE_NAME);

                    FileOutputStream fop = new FileOutputStream(localBackup);
                    mDriveService.files().get(id).executeMediaAndDownloadTo(fop);
                    fop.flush();
                    fop.close();
                    return FileUtil.getStringFromFile(localBackup);
                }
                return null;
            }
        }).addOnSuccessListener(l).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String fetchBackupFileId() {
        FileList result;
        try {
            if (setupDriveService()) {
                result = mDriveService.files()
                        .list()
                        .setQ(String.format("name = '%s'", BACKUP_FILE_NAME))
                        .execute();
                List<File> files = result.getFiles();
                if (files != null && files.size() > 0) {
                    return files.get(0).getId();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    /*
    public Task<String> createFile() {
        return Tasks.call(mExecutor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                File metadata = new File()
                        .setParents(Collections.singletonList("root"))
                        .setMimeType("text/plain")
                        .setName("Untitled file");

                File googleFile = mDriveService.files().create(metadata).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }

                return googleFile.getId();
            }
        });
    }
    */

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    /*
    public Task<Pair<String, String>> readFile(final String fileId) {
        return Tasks.call(mExecutor, new Callable<Pair<String, String>>() {
            @Override
            public Pair<String, String> call() throws Exception {
                // Retrieve the metadata as a File object.
                File metadata = mDriveService.files().get(fileId).execute();
                String name = metadata.getName();

                // Stream the file contents to a String.
                InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder stringBuilder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }
    */

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    /*
    public Task<Void> saveFile(final String fileId, final String name, final String content) {
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create a File containing any metadata changes.
                File metadata = new File().setName(name);

                // Convert content to an AbstractInputStreamContent instance.
                ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

                // Update the metadata and contents.
                mDriveService.files().update(fileId, metadata, contentStream).execute();
                return null;
            }
        });
    }
    */

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    /*
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, new Callable<FileList>() {
            @Override
            public FileList call() throws Exception {
                return mDriveService.files().list().setSpaces("drive").execute();
            }
        });
    }
    */

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    /*
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }
    */

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    /*
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            final ContentResolver contentResolver, final Uri uri) {

        return Tasks.call(mExecutor, new Callable<Pair<String, String>>() {
            @Override
            public Pair<String, String> call() throws Exception {
                // Retrieve the document's display name from its metadata.
                String name;
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
                cursor.close();

                // Read the document's contents as a String.
                String content;
                InputStream is = contentResolver.openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();

                return Pair.create(name, content);
            }
        });
    }
    */
}

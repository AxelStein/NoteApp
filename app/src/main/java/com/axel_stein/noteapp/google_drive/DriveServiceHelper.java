package com.axel_stein.noteapp.google_drive;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.LogHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.axel_stein.domain.utils.TextUtil.isEmpty;
import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class DriveServiceHelper {
    private final Context mContext;
    private Drive mDriveService;

    public DriveServiceHelper(Context context) {
        mContext = context;
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

    public void signOut(@NonNull OnSuccessListener<Void> l, @NonNull OnFailureListener f) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA), new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(mContext, signInOptions);
        client.signOut().addOnSuccessListener(l).addOnFailureListener(f);
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

    public void uploadFile(final String fileName, final String content, @NonNull OnSuccessListener<Void> s, @NonNull OnFailureListener f) {
        Tasks.call(Executors.newSingleThreadExecutor(), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                uploadFileSync(fileName, content);
                return null;
            }
        }).addOnSuccessListener(s).addOnFailureListener(f);
    }

    public void uploadFileSync(String fileName, String content) throws IOException {
        LogHelper.logVerbose("saveFileSync", fileName);

        File metadata = new File().setName(fileName);
        ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

        if (setupDriveService()) {
            String id = getFileId(fileName);
            File file;
            if (isEmpty(id)) {
                metadata.setParents(Collections.singletonList("appDataFolder"));
                file = mDriveService.files().create(metadata, contentStream).setFields("id").execute();
            } else {
                file = mDriveService.files().update(id, metadata, contentStream).execute();
            }
            LogHelper.logVerbose("saveFileSync", fileName, file.getId());
        }
    }

    public void getFileModifiedDate(final String fileName, @NonNull OnSuccessListener<Long> l, @NonNull OnFailureListener f) {
        Tasks.call(Executors.newSingleThreadExecutor(), new Callable<Long>() {
            @Override
            public Long call() {
                return getFileModifiedDateSync(fileName);
            }
        }).addOnSuccessListener(l).addOnFailureListener(f);
    }

    public Long getFileModifiedDateSync(final String fileName) {
        String id = getFileId(fileName);
        if (notEmpty(id) && setupDriveService()) {
            try {
                File f = mDriveService.files().get(id).setFields("modifiedTime").execute();
                return f.getModifiedTime().getValue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void downloadFile(final String fileName, @NonNull OnSuccessListener<String> l, @NonNull OnFailureListener f) {
        Tasks.call(Executors.newSingleThreadExecutor(), new Callable<String>() {
            @Override
            public String call() throws Exception {
                return downloadFileSync(fileName);
            }
        }).addOnSuccessListener(l).addOnFailureListener(f);
    }

    @Nullable
    private String downloadFileSync(String fileName) throws IOException {
        LogHelper.logVerbose("downloadFile", fileName);

        String id = getFileId(fileName);
        if (notEmpty(id) && setupDriveService()) {
            InputStream is = mDriveService.files().get(id).executeMediaAsInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
        return null;
    }

    @Nullable
    private String getFileId(String fileName) {
        try {
            if (setupDriveService()) {
                FileList result = mDriveService.files()
                        .list()
                        .setQ(String.format("name = '%s'", fileName))
                        .execute();
                List<File> files = result.getFiles();
                if (files != null && files.size() > 0) {
                    return files.get(0).getId();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
      Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
      contents.
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

    /*
      Updates the file identified by {@code fileId} with the given {@code name} and {@code
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

    /*
      Returns a {@link FileList} containing all the visible files in the user's My Drive.

      <p>The returned list will only contain files visible to this app, i.e. those which were
      created by this app. To perform operations on files not created by the app, the project must
      request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
      Developer's Console</a> and be submitted to Google for verification.</p>
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

    /*
      Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    /*
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }
    */

    /*
      Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
      created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
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

package com.axel_stein.noteapp.settings;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class GoogleDriveHelper {
    private FragmentActivity mActivity;
    private Callback mCallback;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    public interface Callback {
        void startSignInActivity(Intent intent);
        void showMessage(String msg);
    }

    public void init(FragmentActivity activity, Callback callback) {
        mActivity = activity;
        mCallback = callback;
        signInImpl();
    }

    public void signIn() {
        GoogleSignInClient client = buildGoogleSignInClient();
        if (mCallback != null) {
            mCallback.startSignInActivity(client.getSignInIntent());
        }
    }

    public void signInResultOk() {
        if (!signInImpl() && mCallback != null) {
            mCallback.showMessage("Error");
        }
    }

    private boolean signInImpl() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mActivity);
        if (account != null) {
            mDriveClient = Drive.getDriveClient(mActivity, account);
            mDriveResourceClient = Drive.getDriveResourceClient(mActivity, account);

            if (mCallback != null) {
                mCallback.showMessage("Sign in");
            }
        }
        return account != null;
    }

    public void signOut() {
        GoogleSignInClient client = buildGoogleSignInClient();
        client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<Void> task) {
                if (mCallback != null) {
                    mCallback.showMessage("Sign out");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@android.support.annotation.NonNull Exception e) {
                e.printStackTrace();
                if (mCallback != null) {
                    mCallback.showMessage("Error");
                }
            }
        });
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(mActivity, signInOptions);
    }

    public void export(final String backup) {
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        Tasks.whenAll(appFolderTask, createContentsTask).continueWithTask(new Continuation<Void, Task<DriveFile>>() {
            @Override
            public Task<DriveFile> then(@android.support.annotation.NonNull Task<Void> task) throws Exception {
                DriveFolder parent = appFolderTask.getResult();
                DriveContents contents = createContentsTask.getResult();
                OutputStream outputStream = contents.getOutputStream();

                try {
                    Writer writer = new OutputStreamWriter(outputStream);
                    writer.write(backup);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("notes_backup")
                        .setMimeType("text/plain")
                        .build();

                return mDriveResourceClient.createFile(parent, changeSet, contents);
            }
        }).addOnCompleteListener(new OnCompleteListener<DriveFile>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<DriveFile> task) {
                Toast.makeText(mActivity, "COMPLETED", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@android.support.annotation.NonNull Exception e) {
                Log.e("TAG", e.toString());
                Toast.makeText(mActivity, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

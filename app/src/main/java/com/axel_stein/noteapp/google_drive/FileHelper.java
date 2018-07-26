package com.axel_stein.noteapp.google_drive;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.axel_stein.noteapp.google_drive.LogHelper.logError;
import static com.axel_stein.noteapp.google_drive.LogHelper.logVerbose;
import static com.axel_stein.noteapp.google_drive.LogHelper.logWarning;

class FileHelper {

    private FileHelper() {}

    static List<String> downloadStarredFilesSync(@NonNull DriveResourceClient client) throws Exception {
        DriveFolder folder = Tasks.await(client.getAppFolder());

        Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.STARRED, true)).build();
        MetadataBuffer buffer = Tasks.await(client.queryChildren(folder, query));

        List<String> result = new ArrayList<>();
        for (int i = 0; i < buffer.getCount(); i++) {
            Metadata metadata = buffer.get(i);
            logWarning(metadata.getTitle());

            DriveFile file = metadata.getDriveId().asDriveFile();
            result.add(readFileSync(client, file));
        }
        return result;
    }

    static String readFileSync(@NonNull DriveResourceClient client, @NonNull String title) throws Exception {
        return readFileSync(client, downloadFileSync(client, title));
    }

    private static String readFileSync(@NonNull DriveResourceClient client, @Nullable DriveFile file) throws Exception {
        if (file != null) {
            DriveContents contents = Tasks.await(client.openFile(file, DriveFile.MODE_READ_ONLY));
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static DriveFile downloadFileSync(@NonNull DriveResourceClient client, @NonNull String title) throws Exception {
        DriveFolder folder = Tasks.await(client.getAppFolder());

        Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, title)).build();
        MetadataBuffer buffer = Tasks.await(client.queryChildren(folder, query));

        if (buffer.getCount() == 0) {
            return null;
        }

        return buffer.get(0).getDriveId().asDriveFile();
    }

    static void downloadFile(@NonNull final DriveResourceClient client, @NonNull final String title, @NonNull final OnDownloadListener l) {
        logWarning("downloadFile", title);

        client.getAppFolder().addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {
                Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, title)).build();
                client.queryChildren(driveFolder, query).addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer buffer) {
                        if (buffer.getCount() == 0) {
                            l.onDownload(null);
                        } else {
                            Metadata metadata = buffer.get(0);
                            l.onDownload(metadata.getDriveId().asDriveFile());
                        }
                    }
                });
            }
        });
    }

    static void createFile(@NonNull final DriveResourceClient client, final String title, final String data) {
        createFile(client, title, data, false);
    }

    static void createFile(@NonNull final DriveResourceClient client, final String title, final String data, final boolean starred) {
        logWarning("createFile", title);

        final Task<DriveFolder> appFolderTask = client.getAppFolder();
        final Task<DriveContents> createContentsTask = client.createContents();

        Tasks.whenAll(appFolderTask, createContentsTask).continueWithTask(new Continuation<Void, Task<DriveFile>>() {
            @Override
            public Task<DriveFile> then(@android.support.annotation.NonNull Task<Void> task) {
                DriveFolder appFolder = appFolderTask.getResult();
                DriveContents contents = createContentsTask.getResult();

                try {
                    Writer writer = new OutputStreamWriter(contents.getOutputStream());
                    writer.write(data);
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(title)
                        .setMimeType("text/plain")
                        .setStarred(starred)
                        .setViewed()
                        .build();

                return client.createFile(appFolder, changeSet, contents);
            }
        }).addOnSuccessListener(new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile file) {
                logVerbose("created", title);
                // fixme client.addChangeSubscription(file);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logError("Unable to create file", e);
            }
        });
    }

    static void updateFile(@NonNull final DriveResourceClient client, final DriveFile file, final String data) {
        updateFile(client, file, data, false);
    }

    static void updateFile(@NonNull final DriveResourceClient client, final DriveFile file, final String data, final boolean starred) {
        logWarning("updateFile", file.getDriveId().encodeToString());

        Task<DriveContents> openTask = client.openFile(file, DriveFile.MODE_WRITE_ONLY);
        openTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) {
                DriveContents contents = task.getResult();

                try {
                    Writer writer = new OutputStreamWriter(contents.getOutputStream());
                    writer.write(data);
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setLastViewedByMeDate(new Date())
                        .setStarred(starred)
                        .build();

                return client.commitContents(contents, changeSet);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logVerbose("updated", file.getDriveId().encodeToString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logError("Unable to update file", e);
            }
        });
    }

    static void deleteFile(@NonNull final DriveResourceClient client, final DriveFile file) {
        logWarning("deleteFile", file.getDriveId().encodeToString());

        client.delete(file).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logVerbose("deleted", file.getDriveId().encodeToString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logError("Unable to delete file", e);
            }
        });
    }

}

package com.axel_stein.noteapp.backup;

import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;
import com.axel_stein.domain.interactor.backup.QueryBackupFileInteractor;
import com.axel_stein.domain.model.BackupFile;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.backup.BackupContract.View;
import com.axel_stein.noteapp.utils.FileUtil;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class BackupPresenter implements BackupContract.Presenter, SingleObserver<List<BackupFile>> {

    private View mView;

    private List<BackupFile> mFiles;

    @Inject
    QueryBackupFileInteractor mQueryInteractor;

    @Inject
    Function<List<BackupFile>, List<BackupFile>> mFileInfoConverter;

    @Inject
    ImportBackupInteractor mImportBackupInteractor;

    public BackupPresenter() {
        App.getAppComponent().inject(this);
    }

    private void reload() {
        mQueryInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .map(mFileInfoConverter)
                .subscribe(this);
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onSuccess(List<BackupFile> files) {
        mFiles = files;
        if (mView != null) {
            mView.setItems(mFiles);
        }
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onCreateView(View view) {
        mView = checkNotNull(view);
        if (mFiles != null) {
            mView.setItems(mFiles);
        } else {
            reload();
        }
    }

    @Override
    public void onDestroyView() {
        mView = null;
    }

    @Override
    public void onFileClick(BackupFile file) {
        importFile(file);
    }

    @Override
    public void addFile() {

    }

    @Override
    public void importFile(BackupFile file) {
        try {
            mImportBackupInteractor.execute(FileUtil.getStringFromFile(file.getFile()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            if (mView != null) {
                                mView.showImportDialog();
                            }
                        }

                        @Override
                        public void onComplete() {
                            if (mView != null) {
                                mView.dismissImportDialog();
                            }

                            EventBusHelper.showMessage(R.string.msg_import_success);
                            EventBusHelper.recreate();
                            EventBusHelper.updateNoteList(false, true);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            if (mView != null) {
                                mView.dismissImportDialog();
                            }

                            e.printStackTrace();
                            EventBusHelper.showMessage(R.string.error);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            EventBusHelper.showMessage(R.string.error);
        }
    }

    @Override
    public void renameFile(BackupFile file, String name) {

    }

    @Override
    public void deleteFile(BackupFile file) {
        if (file.getFile().delete()) {
            reload();
        } else {
            EventBusHelper.showMessage(R.string.error);
        }
    }

}

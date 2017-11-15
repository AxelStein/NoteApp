package com.axel_stein.data;

import android.content.Context;

import com.axel_stein.data.utils.FileUtil;
import com.axel_stein.domain.repository.BackupFileRepository;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppBackupFileRepository implements BackupFileRepository {

    private File mDir;

    public AppBackupFileRepository(Context context) {
        mDir = context.getFilesDir();
    }

    @Override
    public File create(String data) {
        String fileName = "notes " + new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.ROOT).format(new Date());
        return create(data, fileName);
    }

    @Override
    public File create(String data, String name) {
        return FileUtil.writeToFile(mDir, name, data);
    }

    @Override
    public boolean rename(File file, String name) {
        return FileUtil.rename(file, name);
    }

    @Override
    public boolean delete(File file) {
        return FileUtil.delete(file);
    }

    @Override
    public List<File> query() {
        List<File> list = Arrays.asList(mDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isDirectory();
            }
        }));
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                long m1 = f1.lastModified();
                long m2 = f2.lastModified();
                if (m1 > m2) {
                    return -1;
                } else if (m2 > m1) {
                    return 1;
                }
                return f1.getName().compareTo(f2.getName());
            }
        });
        return list;
    }

}

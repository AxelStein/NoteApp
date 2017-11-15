package com.axel_stein.domain.repository;

import java.io.File;
import java.util.List;

public interface BackupFileRepository {

    File create(String data);

    File create(String data, String name);

    boolean rename(File file, String name);

    boolean delete(File file);

    List<File> query();

}

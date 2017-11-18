package com.axel_stein.domain.model;

import java.io.File;

public class BackupFile {

    private String name;

    private String info;

    private File file;

    public BackupFile() {
    }

    public BackupFile(File file) {
        this.file = file;
        if (file != null) {
            name = file.getName();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}

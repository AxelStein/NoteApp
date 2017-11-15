package com.axel_stein.noteapp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class FileUtil {

    public static File writeToFile(File dir, String fileName, String data) {
        File file = new File(dir, fileName);

        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(File file) throws Exception {
        return getStringFromFile(file.getAbsolutePath());
    }

    public static String getStringFromFile(String path) throws Exception {
        File file = new File(path);
        FileInputStream stream = new FileInputStream(file);
        String result = convertStreamToString(stream);
        stream.close();
        return result;
    }

    public static boolean rename(File file, String name) {
        File to = new File(file.getParent(), name);
        return file.renameTo(to);
    }

    public static boolean delete(File file) {
        return file.delete();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format(Locale.ROOT, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}

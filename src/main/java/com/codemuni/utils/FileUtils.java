package com.codemuni.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static boolean ensureRecursiveDir(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdirs();
    }

    public static boolean ensureDirectory(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdir();
    }

    public static boolean ensureDirectory(File dir) {
        return dir.exists() || dir.mkdirs();
    }

    public static boolean ensureDirectory(Path path) {
        File dir = path.toFile();
        return dir.exists() || dir.mkdirs();
    }

    public static boolean ensureFile(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean removeFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile() && file.delete();
    }

    public static boolean isFileExist(String filePath) {
        if (filePath == null) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    public static String[] readAllFilesFromFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return new String[0];

        File[] files = folder.listFiles(File::isFile);
        if (files == null) return new String[0];

        List<String> paths = new ArrayList<>();
        for (File file : files) {
            paths.add(file.getAbsolutePath());
        }

        return paths.toArray(new String[0]);
    }
}

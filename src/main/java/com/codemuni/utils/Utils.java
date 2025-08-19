package com.codemuni.utils;


import com.codemuni.App;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

public class Utils {


    private static final Log log = LogFactory.getLog(Utils.class);

    /**
     * Loads and scales an image icon from the given resource path.
     *
     * @param resourcePath The path to the image (e.g., "/icons/pkcs11.png")
     * @param size         The desired width and height
     * @return Scaled ImageIcon or null if not found
     */
    public static ImageIcon loadScaledIcon(String resourcePath, int size) {
        try {
            URL url = Objects.requireNonNull(App.class.getResource(resourcePath), "Icon not found: " + resourcePath);
            Image image = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (Exception e) {
            log.error("Error loading icon from path: " + resourcePath + " -> " + e.getMessage());
            return null;
        }
    }

    public static String truncateText(String appName, String fullFilePath, int maxLength) {
        if (fullFilePath == null || fullFilePath.isEmpty()) {
            return appName;
        }
        String path = fullFilePath;

        if (fullFilePath.length() > maxLength) {
            // Truncate the middle of the path
            int keepStart = maxLength / 2;
            int keepEnd = maxLength - keepStart - 3; // 3 for "..."
            String start = fullFilePath.substring(0, keepStart);
            String end = fullFilePath.substring(fullFilePath.length() - keepEnd);
            path = start + "..." + end;
        }

        return appName + " – " + path;
    }

}

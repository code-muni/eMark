package com.codemuni;

import com.codemuni.utils.AppConstants;
import com.codemuni.config.AppConfig;
import com.codemuni.config.ConfigManager;
import com.codemuni.utils.FileUtils;
import com.codemuni.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.codemuni.utils.AppConstants.TIMESTAMP_SERVER;

public class AppInitializer {

    private static final Log log = LogFactory.getLog(AppInitializer.class);
    private static boolean initialized = false;

    /**
     * Initializes the application only once.
     */
    public static void initialize() {
        if (initialized) return;

        ensureAppDirectories();

        File configFile = new File(AppConstants.CONFIG_FILE);
        if (!configFile.exists()) {
            initializeDefaultConfig();
        } else {
            log.info("Application already initialized.");
        }

        initialized = true;
    }

    /**
     * Ensures required folders exist.
     */
    private static void ensureAppDirectories() {
        ConfigManager.ensureDirectory(AppConstants.CONFIG_DIR);
    }

    /**
     * Sets up a default config file with empty/default values.
     */
    private static void initializeDefaultConfig() {
        AppConfig defaultConfig = new AppConfig();

        // Set default active store
        defaultConfig.setActiveStore(getDefaultActiveStore());

        // Set default timestamp server
        HashMap<String, String> timestampDetails = new HashMap<>();
        timestampDetails.put("url", TIMESTAMP_SERVER);
        timestampDetails.put("username", "");
        timestampDetails.put("password", "");
        defaultConfig.setTimestampServer(timestampDetails);

        
        // Set default PKCS11 paths based on OS
        if (AppConstants.isLinux) {
            defaultConfig.pkcs11.add("/usr/lib/x86_64-linux-gnu/opensc-pkcs11.so");
            defaultConfig.pkcs11.add("/usr/lib64/opensc-pkcs11.so");
            defaultConfig.softHSM = "/usr/local/lib/softhsm/libsofthsm2.so";
        } else if (AppConstants.isMac) {
            // Common PKCS11 library paths for Mac
            defaultConfig.pkcs11.add("/usr/local/lib/opensc-pkcs11.so");
            defaultConfig.softHSM = "/usr/local/lib/softhsm/libsofthsm2.dylib";
        }

        ConfigManager.writeConfig(defaultConfig);
        log.info("Default config file created with platform-specific PKCS11 paths.");
    }


    private static Map<String, Boolean> getDefaultActiveStore() {
        Map<String, Boolean> activeStore = new HashMap<>();
        activeStore.put(AppConstants.WIN_KEY_STORE, AppConstants.isWindow);
        activeStore.put(AppConstants.PKCS11_KEY_STORE, AppConstants.isLinux);
        activeStore.put(AppConstants.SOFTHSM, AppConstants.isLinux || AppConstants.isMac);

        return activeStore;
    }


}

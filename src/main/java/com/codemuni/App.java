package com.codemuni;

import com.codemuni.config.ConfigManager;
import com.codemuni.gui.DialogUtils;
import com.codemuni.gui.PdfViewerMain;
import com.codemuni.utils.FileUtils;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;

import static com.codemuni.utils.AppConstants.LOGO_PATH;

public class App {

    private static final Log log = LogFactory.getLog(App.class);

    static {
        FlatMacDarkLaf.setup(); // Initial theme setup
    }

    public static Image getAppIcon() {
        return Toolkit.getDefaultToolkit().getImage(App.class.getResource(LOGO_PATH));
    }

    public static void main(String[] args) {
        AppInitializer.initialize();      // initialize folders and config
        configureProxyFromConfig();       // read proxy from config

        SwingUtilities.invokeLater(() -> {
            if (!isJava8()) {
                showJavaVersionErrorAndExit();
                return;
            }

            setupUiDefaults();
            launchApp(args);
        });
    }

    private static boolean isJava8() {
        String version = System.getProperty("java.version", "");
        return version.startsWith("1.8");
    }

    private static void showJavaVersionErrorAndExit() {
        String version = System.getProperty("java.version", "unknown");
        String htmlMessage = "<html><body style='font-family:Segoe UI, sans-serif; font-size:12px; width:350px;'>"
                + "<h2 style='color:#cc0000; margin:0;'>Unsupported Java Version</h2>"
                + "<p style='margin-top:10px;'>This application requires <b>Java 8</b> (version <b>1.8.x</b>) to run."
                + "<br><br>Detected Java version: <span style='color:#007acc;'>" + version + "</span>"
                + "<br><br>Please install Java 8 and try again.</p>"
                + "<p style='margin-top:15px; text-align:right;'>Click <b>OK</b> to exit.</p>"
                + "</body></html>";

        DialogUtils.showHtmlMessage(null, "Unsupported Java Version", htmlMessage, DialogUtils.ERROR_MESSAGE);
        System.exit(1);
    }

    private static void setupUiDefaults() {
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ScrollBar.width", 14);
        UIManager.put("ScrollBar.thumbArc", 14);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
    }

    private static void launchApp(String[] args) {
        PdfViewerMain pdfViewerMain = new PdfViewerMain();
        pdfViewerMain.setVisible(true);

        if (args.length == 1 && FileUtils.isFileExist(args[0])) {
            pdfViewerMain.renderPdfFromPath(args[0]);
        }
    }

    /**
     * Reads proxy from config and sets system properties
     */
    private static void configureProxyFromConfig() {
        Map<String, String> proxy = ConfigManager.getProxySettings();
        String host = proxy.getOrDefault("host", "").trim();
        String port = proxy.getOrDefault("port", "").trim();
        String user = proxy.getOrDefault("username", "").trim();
        String pass = proxy.getOrDefault("password", "").trim();

        if (host.isEmpty() || port.isEmpty()) return;

        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);

        log.info("Proxy configured: " + host + ":" + port);

        if (!user.isEmpty()) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass.toCharArray());
                }
            });
            log.info("Proxy authentication configured.");
        }
    }
}

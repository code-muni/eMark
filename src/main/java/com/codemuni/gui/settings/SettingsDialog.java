package com.codemuni.gui.settings;

import javax.swing.*;
import java.awt.*;

import static com.codemuni.utils.AppConstants.APP_NAME;

public class SettingsDialog extends JDialog {
    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 600;

    public SettingsDialog(JFrame parent) {
        super(parent, APP_NAME+ " - Keystore and Security Settings", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(tabbedPane, BorderLayout.CENTER);

        // Keystore Tab
        KeystoreSettingsPanel keystorePanel = new KeystoreSettingsPanel(parent);
        tabbedPane.addTab("Keystore", keystorePanel);

        // Security Tab
        tabbedPane.addTab("Security", new SecuritySettingsPanel());

        // About Tab
        tabbedPane.addTab("About", new AboutPanel());
    }
}

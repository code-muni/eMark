package com.codemuni.gui.settings;

import com.codemuni.config.ConfigManager;
import com.codemuni.utils.Utils;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

public class SecuritySettingsPanel extends JPanel {

    private static final int FIELD_HEIGHT = 30;
    private static final Color DARK_BG = new Color(40, 40, 40);
    private static final Color BORDER_COLOR = new Color(90, 90, 90);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 13);

    private JTextField timestampField, tsaUsernameField, hostField, portField, usernameField;
    private JPasswordField tsaPasswordField, passwordField;

    public SecuritySettingsPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel leftPanel = createLeftPanel();

        // Make leftPanel span both columns and take full width
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 20, 20, 20); // Consistent padding
        add(leftPanel, gbc);

        loadSavedConfig();
    }

    private static JButton getToggleButton(JPasswordField passwordField, ImageIcon closedEyeIcon, ImageIcon openEyeIcon) {
        JButton toggle = new JButton(closedEyeIcon);
        toggle.setPreferredSize(new Dimension(36, FIELD_HEIGHT));
        toggle.setFocusPainted(false);
        toggle.setOpaque(false);

        toggle.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                // Currently visible → hide it
                passwordField.setEchoChar('•');
                toggle.setIcon(closedEyeIcon);
            } else {
                // Currently hidden → show it
                passwordField.setEchoChar((char) 0);
                toggle.setIcon(openEyeIcon);
            }
        });
        return toggle;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Increase width from 480 to ~780 or use dynamic layout
        panel.setPreferredSize(new Dimension(780, 500)); // Expanded width
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(Box.createVerticalGlue());
        panel.add(createTimestampSection());
        panel.add(Box.createVerticalStrut(20));
        panel.add(createProxySection());
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createTimestampSection() {
        JPanel wrapper = createTitledPanel("Timestamp Server");
        wrapper.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        timestampField = createTextField("Enter Timestamp URL", "URL of the timestamp server");
        JPanel urlWrapper = createLabeledField("Timestamp URL", timestampField);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        wrapper.add(urlWrapper, gbc);

        tsaUsernameField = createTextField("Enter Username", "Username for timestamp server");
        JPanel usernameWrapper = createLabeledField("Username", tsaUsernameField);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        wrapper.add(usernameWrapper, gbc);

        tsaPasswordField = createPasswordField("Password for timestamp server");
        JPanel passwordWrapper = createLabeledField("Password", wrapPasswordFieldWithToggle(tsaPasswordField));
        gbc.gridx = 1;
        wrapper.add(passwordWrapper, gbc);

        JButton timestampSaveButton = createButton("Save", new Dimension(150, FIELD_HEIGHT));
        timestampSaveButton.addActionListener(e -> saveTimestampConfig());
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(timestampSaveButton, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        wrapper.add(buttonPanel, gbc);

        return wrapper;
    }

    private JPanel createProxySection() {
        JPanel wrapper = createTitledPanel("Proxy Settings");
        wrapper.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        hostField = createTextField("Enter Proxy Host", "Host address of the proxy server");
        JPanel hostWrapper = createLabeledField("Proxy Host", hostField);
        gbc.gridx = 0;
        gbc.gridy = 0;
        wrapper.add(hostWrapper, gbc);

        portField = createTextField("Enter Proxy Port", "Port number of the proxy server");
        JPanel portWrapper = createLabeledField("Port", portField);
        gbc.gridx = 1;
        wrapper.add(portWrapper, gbc);

        usernameField = createTextField("Enter Username", "Username for proxy authentication");
        JPanel usernameWrapper = createLabeledField("Username", usernameField);
        gbc.gridx = 0;
        gbc.gridy = 1;
        wrapper.add(usernameWrapper, gbc);

        passwordField = createPasswordField("Password for proxy server");
        JPanel passwordWrapper = createLabeledField("Password", wrapPasswordFieldWithToggle(passwordField));
        gbc.gridx = 1;
        wrapper.add(passwordWrapper, gbc);

        JButton proxySaveButton = createButton("Save", new Dimension(150, FIELD_HEIGHT));
        proxySaveButton.addActionListener(e -> saveProxyConfig());
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(proxySaveButton, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        wrapper.add(buttonPanel, gbc);

        return wrapper;
    }

    private JTextField createTextField(String placeholder, String tooltip) {
        JTextField field = new JTextField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.setForeground(Color.LIGHT_GRAY);
        field.setCaretColor(Color.WHITE);
        field.setToolTipText(tooltip);
        field.setPreferredSize(new Dimension(Short.MAX_VALUE, FIELD_HEIGHT));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private JPasswordField createPasswordField(String tooltip) {
        JPasswordField field = new JPasswordField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, tooltip);
        field.setForeground(Color.LIGHT_GRAY);
        field.setCaretColor(Color.WHITE);
        field.setPreferredSize(new Dimension(Short.MAX_VALUE, FIELD_HEIGHT));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private JPanel wrapPasswordFieldWithToggle(JPasswordField passwordField) {
        // Load icons from resources
        ImageIcon closedEyeIcon = Utils.loadScaledIcon("/icons/eye_closed.png", 20);
        ImageIcon openEyeIcon = Utils.loadScaledIcon("/icons/eye_open.png", 20);

        JButton toggle = getToggleButton(passwordField, closedEyeIcon, openEyeIcon);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.add(passwordField, BorderLayout.CENTER);
        panel.add(toggle, BorderLayout.EAST);
        return panel;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.LIGHT_GRAY);
        label.setBorder(new EmptyBorder(0, 2, 4, 2));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(field);
        return panel;
    }

    private JButton createButton(String text, Dimension size) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.setOpaque(true); // Make sure background is visible

        // Hover effect colors
        Color hoverColor = new Color(60, 90, 140);
        Color defaultBg = button.getBackground();

        // Add hover listener
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(defaultBg);
            }
        });

        return button;
    }


    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                SECTION_FONT,
                Color.LIGHT_GRAY));
        return panel;
    }

    private void loadSavedConfig() {
        Map<String, String> ts = ConfigManager.getTimestampServer();
        timestampField.setText(ts.getOrDefault("url", ""));
        tsaUsernameField.setText(ts.getOrDefault("username", ""));
        tsaPasswordField.setText(ts.getOrDefault("password", ""));

        Map<String, String> proxy = ConfigManager.getProxySettings();
        hostField.setText(proxy.getOrDefault("host", ""));
        portField.setText(proxy.getOrDefault("port", ""));
        usernameField.setText(proxy.getOrDefault("username", ""));
        passwordField.setText(proxy.getOrDefault("password", ""));
    }

    private void saveTimestampConfig() {
        String url = timestampField.getText().trim();
        String username = tsaUsernameField.getText().trim();
        String password = new String(tsaPasswordField.getPassword());
        boolean success = ConfigManager.setTimestampServer(url, username, password);

        JOptionPane.showMessageDialog(this,
                success ? "Timestamp settings saved successfully." : "Failed to save timestamp settings.",
                "Timestamp Settings",
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void saveProxyConfig() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        boolean success = ConfigManager.setProxySettings(host, port, username, password);

        JOptionPane.showMessageDialog(this,
                success ? "Proxy settings saved successfully." : "Failed to save proxy settings.",
                "Proxy Settings",
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}
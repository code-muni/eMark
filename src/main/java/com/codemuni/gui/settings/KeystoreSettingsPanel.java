package com.codemuni.gui.settings;

import com.codemuni.config.ConfigManager;
import com.codemuni.gui.ConfirmWordDialog;
import com.codemuni.gui.DialogUtils;
import com.codemuni.utils.AppConstants;
import com.codemuni.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class KeystoreSettingsPanel extends JPanel {
    private static final int PATH_PANEL_HEIGHT = 40;
    private static final int PATH_FIELD_WIDTH = 220;
    private static final Color BORDER_COLOR = new Color(100, 100, 100);
    private static final Color REMOVE_BUTTON_COLOR = new Color(198, 25, 25, 69);

    private final JPanel pkcs11Container = new JPanel();
    private final List<JPanel> pkcs11PathPanels = new ArrayList<>();
    private final JFrame parentDialog;
    private JCheckBox winStoreCheck;
    private JCheckBox pkcs11Check;
    private JCheckBox pfxCheck;

    public KeystoreSettingsPanel(JFrame parentDialog) {
        this.parentDialog = parentDialog;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initUI();
        loadInitialPaths();
    }

    private void initUI() {
        add(createKeystoreCheckboxPanel(), BorderLayout.NORTH);
        add(createPKCS11PathPanel(), BorderLayout.CENTER);
    }

    /**
     * Creates the top checkbox panel with proper titled border alignment
     */
    private JPanel createKeystoreCheckboxPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                "Active Keystores",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                panel.getFont().deriveFont(Font.BOLD)
        ));

        Map<String, Boolean> activeStores = ConfigManager.getActiveStore();

        winStoreCheck = new JCheckBox("Win Store", activeStores.getOrDefault(AppConstants.WIN_KEY_STORE, false));
        pkcs11Check = new JCheckBox("PKCS#11 Store", activeStores.getOrDefault(AppConstants.PKCS11_KEY_STORE, false));
        pfxCheck = new JCheckBox("PFX Store", activeStores.getOrDefault(AppConstants.SOFTHSM, false));

        winStoreCheck.setEnabled(AppConstants.isWindow);
        if (!AppConstants.isWindow) winStoreCheck.setToolTipText("Windows Store is only supported on Windows.");

        setupCheckboxValidation();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        panel.add(winStoreCheck, gbc);

        gbc.gridx++;
        panel.add(pkcs11Check, gbc);

        gbc.gridx++;
        panel.add(pfxCheck, gbc);

        return panel;
    }

    /**
     * Creates the PKCS#11 path management panel with proper titled border
     */
    private JPanel createPKCS11PathPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "PKCS#11 Path Management",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                panel.getFont().deriveFont(Font.BOLD)
        ));

        JButton browseButton = new JButton("Browse PKCS#11 File");
        browseButton.putClientProperty("JButton.buttonType", "roundRect");
        browseButton.addActionListener(e -> browseAndAddPath(this::addPkcs11Path));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(browseButton);
        panel.add(top, BorderLayout.NORTH);

        pkcs11Container.setLayout(new BoxLayout(pkcs11Container, BoxLayout.Y_AXIS));
        pkcs11Container.setAlignmentY(Component.TOP_ALIGNMENT);
        pkcs11Container.setBorder(new EmptyBorder(0, 8, 0, 8)); // 8px horizontal padding

        JScrollPane scrollPane = new JScrollPane(pkcs11Container) {
            @Override
            public void doLayout() {
                super.doLayout();
                // Always scroll to top if content is smaller than viewport
                if (getViewport().getView() != null &&
                        getViewport().getView().getPreferredSize().height <= getViewport().getHeight()) {
                    getViewport().setViewPosition(new Point(0, 0));
                }
            }
        };

        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Also force top alignment in case of revalidation
        scrollPane.getViewport().setAlignmentY(Component.TOP_ALIGNMENT);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void setupCheckboxValidation() {
        ItemListener listener = e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED &&
                    !winStoreCheck.isSelected() && !pkcs11Check.isSelected() && !pfxCheck.isSelected()) {
                DialogUtils.showWarning(parentDialog, "Validation Error",
                        "<html><body>At least one <strong>keystore</strong> type must be active.");
                ((JCheckBox) e.getSource()).setSelected(true);
            } else {
                ConfigManager.setActiveStore(AppConstants.WIN_KEY_STORE, winStoreCheck.isSelected());
                ConfigManager.setActiveStore(AppConstants.PKCS11_KEY_STORE, pkcs11Check.isSelected());
                ConfigManager.setActiveStore(AppConstants.SOFTHSM, pfxCheck.isSelected());
            }
        };

        winStoreCheck.addItemListener(listener);
        pkcs11Check.addItemListener(listener);
        pfxCheck.addItemListener(listener);
    }

    private void loadInitialPaths() {
        for (String path : ConfigManager.getPKCS11Paths()) {
            addPkcs11Path(path);
        }
    }

    private void browseAndAddPath(Consumer<String> addPathMethod) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (AppConstants.isLinux) {
            chooser.setFileFilter(new FileNameExtensionFilter("PKCS#11 Library (*.so)", "so"));
        } else if (AppConstants.isMac) {
            chooser.setFileFilter(new FileNameExtensionFilter("PKCS#11 Library (*.dylib)", "dylib"));
        } else {
            chooser.setFileFilter(new FileNameExtensionFilter("PKCS#11 Library (*.dll)", "dll"));
        }

        if (chooser.showOpenDialog(parentDialog) == JFileChooser.APPROVE_OPTION) {
            addPathMethod.accept(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void addPkcs11Path(String path) {
        if (ConfigManager.addPKCS11Path(path)) {
            addPath(path, pkcs11Container, pkcs11PathPanels);
        }
    }

    private void addPath(String path, JPanel container, List<JPanel> pathPanels) {
        JPanel pathPanel = createPathPanel(path, container, pathPanels);
        container.add(pathPanel);
        container.add(Box.createVerticalStrut(6));
        pathPanels.add(pathPanel);
        container.revalidate();
        container.repaint();
    }

    private JPanel createPathPanel(String path, JPanel container, List<JPanel> pathPanels) {
        JPanel pathPanel = new JPanel(new BorderLayout());
        pathPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, PATH_PANEL_HEIGHT));
        pathPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(81, 81, 81), 1, true),
                new EmptyBorder(8, 8, 4, 8)));
        pathPanel.setOpaque(true);

        JTextField pathField = new JTextField(Utils.truncateText("", path, 40));
        pathField.setPreferredSize(new Dimension(PATH_FIELD_WIDTH, PATH_PANEL_HEIGHT - 10));
        pathField.setEditable(false);
        pathField.setOpaque(false);
        pathField.setBorder(null);
        pathField.setFont(pathField.getFont().deriveFont(14f));

        JButton removeBtn = new JButton("Remove");
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setBackground(REMOVE_BUTTON_COLOR);
        removeBtn.putClientProperty("JButton.buttonType", "roundRect");
        removeBtn.setFont(removeBtn.getFont().deriveFont(13f));

        removeBtn.addActionListener(e -> {
            ConfirmWordDialog confirmDialog = new ConfirmWordDialog(parentDialog, "DELETE");
            confirmDialog.setVisible(true);

            if (!confirmDialog.isConfirmed()) return;

            container.remove(pathPanel);
            pathPanels.remove(pathPanel);

            ConfigManager.removePKCS11Path(path);

            container.revalidate();
            container.repaint();
        });

        pathPanel.add(pathField, BorderLayout.CENTER);
        pathPanel.add(removeBtn, BorderLayout.EAST);
        return pathPanel;
    }
}

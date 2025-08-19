package com.codemuni.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Predicate;

import static com.codemuni.gui.GuiConstraints.FIELD_HEIGHT;
import static com.codemuni.gui.GuiConstraints.INPUT_FIELD_CONFIG;

public class PasswordDialog extends JDialog {
    private final JPasswordField inputField;
    private final JLabel messageLabel;
    private final JButton openDocumentButton;
    private final String defaultMessage;
    private boolean wasClosedByUser = false;
    private boolean confirmed = false;
    private Predicate<String> validator;
    private boolean hasErrorMessage = false;

    private Timer shakeTimer, stopTimer;

    public PasswordDialog(Window parent,
                          String title,
                          String message,
                          String placeholder,
                          String openText,
                          String cancelText) {
        super(parent, title != null ? title : "Authentication Required", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        this.inputField = new JPasswordField(15);
        this.inputField.setEchoChar('•');
        this.inputField.setPreferredSize(new Dimension(200, FIELD_HEIGHT));
        this.inputField.putClientProperty(FlatClientProperties.STYLE, INPUT_FIELD_CONFIG);

        if (placeholder != null) {
            inputField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        }

        this.messageLabel = new JLabel(message != null ? message : "Please enter your credentials");
        this.messageLabel.setHorizontalAlignment(SwingConstants.LEFT);

        this.defaultMessage = this.messageLabel.getText();
        this.openDocumentButton = new JButton(openText != null ? openText : "Open Document");

        // Autofocus input
        inputField.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && inputField.isShowing()) {
                SwingUtilities.invokeLater(inputField::requestFocusInWindow);
            }
        });

        initUI(cancelText != null ? cancelText : "Cancel");

        pack();
        setMinimumSize(new Dimension(UIScale.scale(300), getPreferredSize().height));
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                wasClosedByUser = true;
                confirmed = false;
            }
        });
    }

    private void initUI(String cancelText) {
        JPanel content = new JPanel(new BorderLayout(0, UIScale.scale(10)));
        content.setBorder(BorderFactory.createEmptyBorder(
                UIScale.scale(12), UIScale.scale(12),
                UIScale.scale(12), UIScale.scale(12)));
        setContentPane(content);

        messageLabel.setFont(UIManager.getFont("Label.font").deriveFont(UIScale.scale(13f)));

        // ✅ Use GridBagLayout to align message and field
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        formPanel.add(messageLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(UIScale.scale(6), 0, 0, 0); // spacing between label and input
        formPanel.add(inputField, gbc);

        content.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

        JButton cancelButton = new JButton(cancelText);
        openDocumentButton.setEnabled(false);

        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(openDocumentButton);
        buttonsPanel.add(Box.createHorizontalStrut(UIScale.scale(8)));
        buttonsPanel.add(cancelButton);

        content.add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(openDocumentButton);

        // Input listener
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String value = getValue().trim();

                if (hasErrorMessage) {
                    inputField.putClientProperty(FlatClientProperties.OUTLINE, null);
                }

                boolean isValid = !value.isEmpty() && (validator == null || validator.test(value));
                openDocumentButton.setEnabled(isValid);

                getRootPane().setDefaultButton(openDocumentButton);
            }

            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });

        // Authenticate action
        openDocumentButton.addActionListener(e -> {
            String value = getValue();
            if (validator != null && !validator.test(value)) {
                showInvalidMessage(null);
                return;
            }
            confirmed = true;
            clearErrorUI();
            dispose();
        });

        // Cancel action
        cancelButton.addActionListener(e -> {
            confirmed = false;
            clearErrorUI();
            dispose();
        });

        // ESC action
        getRootPane().registerKeyboardAction(
                e -> {
                    confirmed = false;
                    clearErrorUI();
                    dispose();
                },
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public String getValue() {
        return new String(inputField.getPassword());
    }

    public boolean wasClosedByUser() {
        return wasClosedByUser;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setValidator(Predicate<String> validator) {
        this.validator = validator;
    }

    public void showInvalidMessage(String customMessage) {
        hasErrorMessage = true;
        inputField.putClientProperty(FlatClientProperties.OUTLINE, "error");

        messageLabel.setText("<html><font color='#CC0000'>" +
                (customMessage != null ? customMessage : "Invalid password — try again.") +
                "</font></html>");

        startShakeAnimation();

        SwingUtilities.invokeLater(() -> {
            inputField.requestFocusInWindow();
            inputField.selectAll();
        });
    }

    private void startShakeAnimation() {
        if (shakeTimer != null && shakeTimer.isRunning()) shakeTimer.stop();
        if (stopTimer != null && stopTimer.isRunning()) stopTimer.stop();

        final long start = System.currentTimeMillis();
        shakeTimer = new Timer(16, ev -> {
            int t = (int) ((System.currentTimeMillis() - start) / 16);
            int offset = (int) (Math.sin(t * 0.6) * UIScale.scale(3));
            inputField.setBorder(BorderFactory.createEmptyBorder(
                    UIScale.scale(4), UIScale.scale(4 + offset),
                    UIScale.scale(4), UIScale.scale(4 - offset)));
            inputField.revalidate();
            inputField.repaint();
        });
        shakeTimer.start();

        stopTimer = new Timer(300, ev -> {
            shakeTimer.stop();
            inputField.setBorder(BorderFactory.createEmptyBorder(
                    UIScale.scale(4), UIScale.scale(4),
                    UIScale.scale(4), UIScale.scale(4)));
            inputField.revalidate();
            inputField.repaint();
        });
        stopTimer.setRepeats(false);
        stopTimer.start();
    }

    private void clearErrorUI() {
        hasErrorMessage = false;
        inputField.putClientProperty(FlatClientProperties.OUTLINE, null);
        messageLabel.setText(defaultMessage);
    }
}

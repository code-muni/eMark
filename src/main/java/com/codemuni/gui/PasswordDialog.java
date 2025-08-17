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

public class PasswordDialog extends JDialog {
    private final JPasswordField inputField;
    private final JLabel messageLabel;
    private final JButton openDocumentButton;
    private final String defaultMessage;
    private JPanel inputWrapper;
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
        this.inputField.putClientProperty(FlatClientProperties.STYLE,
                "arc:8;" +
                        "showClearButton:true;" +
                        "showRevealButton:true;" +
                        "font:$medium.font");

        if (placeholder != null) {
            inputField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        }

        this.messageLabel = new JLabel(message != null ? message : "Please enter your credentials");
        this.defaultMessage = this.messageLabel.getText();
        this.openDocumentButton = new JButton(openText != null ? openText : "Open Document");

        // Autofocus
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
        content.add(messageLabel, BorderLayout.NORTH);

        inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setOpaque(false);
        inputWrapper.setBorder(BorderFactory.createEmptyBorder(
                UIScale.scale(4), UIScale.scale(4),
                UIScale.scale(4), UIScale.scale(4)));
        inputWrapper.add(inputField, BorderLayout.CENTER);
        content.add(inputWrapper, BorderLayout.CENTER);

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

        // Input change listener — reset outline & button, keep error text
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String value = getValue().trim();

                // Always reset the red outline if user starts typing
                if (hasErrorMessage) {
                    inputField.putClientProperty(FlatClientProperties.OUTLINE, null);
                }

                boolean isValid = !value.isEmpty() && (validator == null || validator.test(value));
                openDocumentButton.setEnabled(isValid);

                // Restore default button behavior after error
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

        // Open action
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
            inputWrapper.setBorder(BorderFactory.createEmptyBorder(
                    UIScale.scale(4), UIScale.scale(4 + offset),
                    UIScale.scale(4), UIScale.scale(4 - offset)));
            inputWrapper.revalidate();
            inputWrapper.repaint();
        });
        shakeTimer.start();

        stopTimer = new Timer(300, ev -> {
            shakeTimer.stop();
            inputWrapper.setBorder(BorderFactory.createEmptyBorder(
                    UIScale.scale(4), UIScale.scale(4),
                    UIScale.scale(4), UIScale.scale(4)));
            inputWrapper.revalidate();
            inputWrapper.repaint();
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

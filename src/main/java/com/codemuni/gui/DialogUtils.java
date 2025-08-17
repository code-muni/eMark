package com.codemuni.gui;

import com.codemuni.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

public class DialogUtils {

    public static final int INFO_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
    public static final int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
    public static final int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
    public static final int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;

    private static final String PREF_KEY = "showSignModeMessage";

    /**
     * Shows an informational dialog.
     */
    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, INFO_MESSAGE);
    }

    /**
     * Shows a warning dialog.
     */
    public static void showWarning(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, WARNING_MESSAGE);
    }

    /**
     * Shows an error dialog.
     */
    public static void showError(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, ERROR_MESSAGE);
    }

    /**
     * Shows a confirmation dialog with Yes/No options.
     * Returns true if user selects Yes.
     */
    public static boolean confirmYesNo(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a confirmation dialog with Yes/No/Cancel options.
     * Returns JOptionPane.YES_OPTION, NO_OPTION, or CANCEL_OPTION.
     */
    public static int confirmYesNoCancel(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION, QUESTION_MESSAGE);
    }

    /**
     * Shows a dialog with HTML content.
     */
    public static void showHtmlMessage(Component parent, String title, String htmlMessage, int messageType) {
        JLabel label = new JLabel(htmlMessage);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(parent, label, title, messageType);
    }

    /**
     * Shows an exception dialog with escaped message.
     */
    public static void showException(Component parent, String title, Exception e) {
        String message = e.getMessage() != null ? escapeHtml(e.getMessage()) : "No message available";
        String html = "<html><div style='color:#ff5555; font-weight:bold;'>An error occurred:</div>"
                + "<div style='margin-top:4px;'>" + message + "</div></html>";
        showHtmlMessage(parent, title, html, ERROR_MESSAGE);
    }

    /**
     * Shows a dialog with options (e.g., OK / Details).
     * Returns selected option index.
     */
    public static int showOptionDialog(Component parent, String htmlMessage, String title, String[] options, int messageType) {
        JLabel label = new JLabel(htmlMessage);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return JOptionPane.showOptionDialog(parent, label, title, JOptionPane.DEFAULT_OPTION,
                messageType, null, options, options[0]);
    }

    /**
     * Escapes basic HTML to prevent formatting issues.
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Show full exception (message + stack trace) in a scrollable dialog.
     */
    public static void showFullExceptionDialog(Component parent, String title, Exception e) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        StringBuilder sb = new StringBuilder();
        sb.append("Exception: ").append(e.getClass().getName()).append("\n");
        sb.append("Message: ").append(e.getMessage() != null ? e.getMessage() : "No message").append("\n\n");

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
        }

        Throwable cause = e.getCause();
        if (cause != null) {
            sb.append("\nCaused by: ").append(cause.getClass().getName()).append(": ")
                    .append(cause.getMessage() != null ? cause.getMessage() : "No message").append("\n");
            for (StackTraceElement element : cause.getStackTrace()) {
                sb.append("    at ").append(element.toString()).append("\n");
            }
        }

        textArea.setText(sb.toString());

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(parent, scrollPane, title, ERROR_MESSAGE);
    }


    /**
     * Shows an error dialog with a summary and option to view detailed stack trace.
     */
    public static void showExceptionWithDetails(Component parent, String title, Exception e) {
        String summaryHtml = "<html><body>"
                + "<div style='color:#ff5555; font-weight:bold; font-size:14px;'>" + title + "</div>"
                + "<div style='margin-top:6px; color:#dddddd;'>"
                + "Exception: " + e.getClass().getSimpleName() + "<br/>"
                + "Message: " + (e.getMessage() != null ? escapeHtml(e.getMessage()) : "No message available")
                + "</div>"
                + "<div style='margin-top:10px;'>Click 'Details' to view technical information.</div>"
                + "</body></html>";

        int choice = showOptionDialog(parent, summaryHtml, title, new String[]{"OK", "Details"}, ERROR_MESSAGE);
        if (choice == 1) { // Details clicked
            showFullExceptionDialog(parent, title + " - Technical Details", e);
        }
    }
    public static boolean showHtmlMessageWithCheckbox(Component parent, String title,
                                                      String htmlMessage, String preferenceKey) {
        Preferences prefs = Preferences.userNodeForPackage(DialogUtils.class);
        if (prefs.getBoolean(preferenceKey, false)) {
            return false;
        }

        // Create the message label with HTML content
        JLabel messageLabel = new JLabel(htmlMessage);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Add horizontal padding

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageLabel, BorderLayout.WEST);

        // 'Don't show again' checkbox
        JCheckBox dontShowAgain = new JCheckBox("Don't show this message again");
        dontShowAgain.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        // OK button
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(75, 28));

        // Mark as default button
        JRootPane rootPane = new JRootPane();
        rootPane.setDefaultButton(okButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.add(okButton);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(dontShowAgain, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Create dialog
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.getContentPane().add(mainPanel);
        dialog.getRootPane().setDefaultButton(okButton); // enter = OK
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        // Keyboard shortcuts
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        okButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);

        // Save preference if checked
        if (dontShowAgain.isSelected()) {
            prefs.putBoolean(preferenceKey, true);
        }

        return true;
    }

}

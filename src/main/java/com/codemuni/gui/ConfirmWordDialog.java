package com.codemuni.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static com.codemuni.gui.GuiConstraints.INPUT_FIELD_CONFIG;

public class ConfirmWordDialog extends JDialog {
    private final JButton confirmButton;
    private boolean confirmed = false;

    public ConfirmWordDialog(Frame parent, String requiredWord) {
        super(parent, "Confirmation Required", true);
        setSize(400, 190);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Main panel with padding
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 10, 20));
        content.setBackground(UIManager.getColor("Panel.background"));
        add(content, BorderLayout.CENTER);

        // Instruction
        JLabel label = new JLabel("<html>Type <b>\"" + requiredWord + "\"</b> to confirm:</html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 14f));
        content.add(label);
        content.add(Box.createVerticalStrut(10));

        // Input field
        JTextField inputField = new JTextField();
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        inputField.putClientProperty("JTextField.placeholderText", requiredWord);
        inputField.putClientProperty(FlatClientProperties.STYLE, INPUT_FIELD_CONFIG);
        inputField.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(inputField);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cancelButton.setFont(label.getFont().deriveFont(Font.PLAIN, 14f));
        cancelButton.setMargin(new Insets(5, 10, 5, 10)); // top, left, bottom, right

        confirmButton = new JButton("Confirm");
        confirmButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        confirmButton.setFont(confirmButton.getFont().deriveFont(Font.BOLD, 15f));
        confirmButton.setBackground(new Color(0x007BFF)); // Bootstrap blue
        confirmButton.setForeground(Color.WHITE); // Text color
        confirmButton.setMargin(new Insets(5, 10, 5, 10)); // top, left, bottom, right
        confirmButton.setEnabled(false);

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(confirmButton);

        add(buttonsPanel, BorderLayout.SOUTH);


        // Listeners for input field if enter is pressed
        inputField.addActionListener(e -> {
            if (requiredWord.equals(inputField.getText().trim())) {
                confirmButton.doClick();
            }
        });

        // Listeners
        inputField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                confirmButton.setEnabled(requiredWord.equals(inputField.getText().trim()));
            }
        });

        confirmButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }

    // Demo main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatMacDarkLaf.setup(); // Set theme

            ConfirmWordDialog dialog = new ConfirmWordDialog(null, "DELETE");
            dialog.setVisible(true);

            JOptionPane.showMessageDialog(null,
                    dialog.isConfirmed() ? "Confirmed ✅" : "Cancelled ❌");
        });
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private abstract static class SimpleDocumentListener implements DocumentListener {
        public abstract void update();

        public void insertUpdate(DocumentEvent e) {
            update();
        }

        public void removeUpdate(DocumentEvent e) {
            update();
        }

        public void changedUpdate(DocumentEvent e) {
            update();
        }
    }
}

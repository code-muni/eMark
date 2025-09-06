package com.codemuni.gui.pdfHandler;

import javax.swing.*;
import java.awt.*;


/**
 * Professional centered placeholder shown before a PDF is loaded.
 * Supports opening PDF via button or drag-and-drop.
 */
public class PlaceholderPanel extends JPanel {
    public PlaceholderPanel(Runnable onOpen) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true); // respects FlatLaf theme
        setBackground(null); // use default background from FlatLaf

        // Title - larger, bold
        JLabel titleLabel = new JLabel("No PDF Loaded");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle - lighter weight, slightly smaller
        JLabel subtitleLabel = new JLabel("Drag and drop a PDF here or click below to open a file");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 14f));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(UIManager.getColor("Label.foreground").darker().darker()); // subtle grayish tone

        // Open PDF button - use FlatLaf default button styling
        JButton openBtn = UiFactory.createButton("Open PDF", null); // null to use FlatLaf default button color
        openBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        openBtn.addActionListener(e -> onOpen.run());

        // Layout spacing
        add(Box.createVerticalGlue());
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 6)));
        add(subtitleLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(openBtn);
        add(Box.createVerticalGlue());
    }
}


package com.codemuni.gui.pdfHandler;

import javax.swing.*;
import java.awt.*;

/**
 * Simple centered placeholder shown before a PDF is loaded.
 */
public class PlaceholderPanel extends JPanel {
    public PlaceholderPanel(Runnable onOpen) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel placeholderLabel = new JLabel("Please Open Document PDF to sign");
        placeholderLabel.setForeground(Color.LIGHT_GRAY);
        placeholderLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton placeholderOpenBtn = UiFactory.createButton("Open PDF", new Color(0x007BFF));
        placeholderOpenBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeholderOpenBtn.addActionListener(e -> onOpen.run());

        add(Box.createVerticalGlue());
        add(placeholderLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(placeholderOpenBtn);
        add(Box.createVerticalGlue());
    }
}

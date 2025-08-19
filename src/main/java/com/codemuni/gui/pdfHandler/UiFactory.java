package com.codemuni.gui.pdfHandler;

import javax.swing.*;
import java.awt.*;

/**
 * Small helpers to reduce UI boilerplate.
 */
public final class UiFactory {
    private UiFactory() {
    }

    public static JButton createButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setMargin(new Insets(5, 10, 5, 10));
        if (bg != null) button.setBackground(bg);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JPanel wrapLeft(JComponent comp) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        panel.add(comp);
        return panel;
    }

    public static JPanel wrapRight(JComponent comp) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        panel.add(comp);
        return panel;
    }
}

package com.codemuni.gui.pdfHandler;

import com.formdev.flatlaf.ui.FlatUIUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Self-contained top bar with:
 * - Open PDF button
 * - Settings button
 * - Begin/Cancel Sign button
 * - Page info label
 */
public class TopBarPanel extends JPanel {
    private static final String OPEN_PDF_TEXT = "Open PDF";
    private static final String BEGIN_SIGN_TEXT = "Begin Sign";
    private static final String CANCEL_SIGN_TEXT = "Cancel Sign";
    private static final Log log = LogFactory.getLog(TopBarPanel.class);

    private final JButton openBtn;
    private final JButton signBtn;
    private final JButton settingsBtn;
    private final JLabel pageInfoLabel;

    private boolean signMode = false;

    public TopBarPanel(Runnable onOpen, Runnable onSettings, Runnable onToggleSign) {
        super(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(FlatUIUtils.getUIColor("Panel.background", Color.WHITE));

        openBtn = UiFactory.createButton(OPEN_PDF_TEXT, new Color(0x007BFF));
        openBtn.addActionListener(e -> onOpen.run());

        signBtn = UiFactory.createButton(BEGIN_SIGN_TEXT, new Color(0x28A745));
        signBtn.setVisible(false);
        signBtn.addActionListener(e -> {
            signMode = !signMode;
            updateSignButtonText();
            onToggleSign.run();
        });

        settingsBtn = UiFactory.createButton("Settings", new Color(0x6C757D));
        settingsBtn.addActionListener(e -> onSettings.run());

        pageInfoLabel = new JLabel("");
        pageInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(pageInfoLabel);
        centerPanel.add(signBtn);

        add(UiFactory.wrapLeft(openBtn), BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(UiFactory.wrapRight(settingsBtn), BorderLayout.EAST);
    }

    public void setPageInfoText(String text) {
        pageInfoLabel.setText(text);
    }

    public void setSignButtonVisible(boolean visible) {
        signBtn.setVisible(visible);
    }

    public void setInteractiveEnabled(boolean enabled) {
        openBtn.setEnabled(enabled);
        settingsBtn.setEnabled(enabled);
        signBtn.setEnabled(enabled);

        setSignMode(!enabled); // Update button text
    }

    public void setLoading(boolean loading) {
        openBtn.setText(loading ? "Opening PDF..." : OPEN_PDF_TEXT);
        setInteractiveEnabled(!loading);
    }

    public void setSignMode(boolean enabled) {
        this.signMode = enabled;
        updateSignButtonText();
    }

    private void updateSignButtonText() {
        signBtn.setText(signMode ? CANCEL_SIGN_TEXT : BEGIN_SIGN_TEXT);
    }
}

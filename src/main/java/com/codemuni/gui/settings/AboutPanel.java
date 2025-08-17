package com.codemuni.gui.settings;

import com.codemuni.gui.DialogUtils;
import com.codemuni.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Year;

import static com.codemuni.utils.AppConstants.*;

public class AboutPanel extends JPanel {

    private static final Log log = LogFactory.getLog(AboutPanel.class);
    private static final int LOGO_SIZE = 90;
    private static final int CARD_PADDING = 5;
    private static final Color HOVER_COLOR = new Color(100, 149, 237); // Cornflower blue for hover

    public AboutPanel() {
        super(new BorderLayout());
        setBorder(new EmptyBorder(40, 40, 50, 40));

        // --- Main Content Panel (Centered Card) ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.setMaximumSize(new Dimension(500, 600));

        // Add padding border around card
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // --- Logo ---
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(Utils.loadScaledIcon(LOGO_PATH, LOGO_SIZE));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // --- Title ---
        JLabel titleLabel = new JLabel(APP_NAME);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 26));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Version ---
        JLabel versionLabel = new JLabel("Version " + APP_VERSION);
        versionLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        versionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));

        // --- Description ---
        JEditorPane descriptionPane = getDescriptionPane();

        // --- Links Panel ---
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        linksPanel.setOpaque(false);
        linksPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        linksPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        linksPanel.add(createStyledLink("🌐 Visit Website", APP_WEBSITE));
        linksPanel.add(createStyledLink("📄 View License", APP_LICENSE_URL));

        // --- Copyright ---
        JLabel copyrightLabel = new JLabel(
                "© " + Year.now().getValue() + " " + APP_AUTHOR + ". All rights reserved."
        );
        copyrightLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        copyrightLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Assemble Content ---
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(logoLabel);
        contentPanel.add(titleLabel);
        contentPanel.add(versionLabel);
        contentPanel.add(descriptionPane);
        contentPanel.add(linksPanel);
        contentPanel.add(copyrightLabel);
        contentPanel.add(Box.createVerticalGlue());

        // --- Centering Wrapper ---
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(contentPanel, gbc);

        add(wrapper, BorderLayout.CENTER);
    }

    private JEditorPane getDescriptionPane() {
        JEditorPane descriptionPane = new JEditorPane("text/html", "");
        descriptionPane.setEditorKit(new HTMLEditorKit() {
            @Override
            public StyleSheet getStyleSheet() {
                StyleSheet styleSheet = super.getStyleSheet();
                styleSheet.addRule("body { text-align: center; color: " +
                        getColorHex(UIManager.getColor("Label.foreground")) +
                        "; font-family: Dialog; font-size: 14px; }");
                styleSheet.addRule("a { color: #8A2BE2; text-decoration: none; }"); // Nice purple link
                styleSheet.addRule("a:hover { text-decoration: underline; }");
                return styleSheet;
            }
        });
        descriptionPane.setText("<html><body>" + APP_DESCRIPTION + "</body></html>");
        descriptionPane.setOpaque(false);
        descriptionPane.setEditable(false);
        descriptionPane.setFocusable(false);
        descriptionPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        descriptionPane.setMaximumSize(new Dimension(400, 60));
        return descriptionPane;
    }

    private JLabel createStyledLink(String text, String url) {
        JLabel link = new JLabel(text);
        Font linkFont = new Font("Dialog", Font.PLAIN, 14);
        link.setFont(linkFont);
        link.setForeground(new Color(100, 149, 237)); // Cornflower blue
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                link.setForeground(HOVER_COLOR.brighter());
                link.setFont(linkFont.deriveFont(Font.BOLD));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                link.setForeground(new Color(100, 149, 237));
                link.setFont(linkFont);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl(url);
            }
        });

        return link;
    }

    private void openUrl(String urlString) {
        try {
            URI uri = new URI(urlString);
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            } else {
                JOptionPane.showMessageDialog(this,
                        "<html>Unable to open browser.<br>Please visit:<br><b>" + urlString + "</b></html>",
                        "Open Link",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (URISyntaxException e) {
            log.error("Invalid URL: " + urlString, e);
            DialogUtils.showError(null, "Error", "Invalid link address.");
        } catch (IOException e) {
            log.error("Failed to open URL: " + urlString, e);
            DialogUtils.showError(null, "Error", "Could not open web browser.");
        } catch (Exception e) {
            log.error("Unexpected error opening URL: " + urlString, e);
            DialogUtils.showError(null, "Error", "An unexpected error occurred.");
        }
    }

    // Utility to convert Color to hex string for HTML
    private String getColorHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // Custom border for rounded corners with outer stroke
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color shadowColor;

        RoundedBorder(int radius, Color shadowColor) {
            this.radius = radius;
            this.shadowColor = shadowColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(shadowColor);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING);
        }

    }
}
package com.codemuni.gui;

import com.codemuni.App;
import com.codemuni.controller.SignerController;
import com.codemuni.exceptions.IncorrectPINException;
import com.codemuni.exceptions.UserCancelledPasswordEntryException;
import com.codemuni.gui.settings.SettingsDialog;
import com.codemuni.utils.Utils;
import com.formdev.flatlaf.ui.FlatUIUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import static com.codemuni.utils.AppConstants.APP_NAME;

public class PdfViewerMain extends JFrame {

    private static final Log log = LogFactory.getLog(PdfViewerMain.class);
    private static final int INITIAL_WIDTH = 950;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 400;
    private static final int RENDER_DPI = 100;
    private static final int DEFAULT_RENDERER_PADDING = 10;
    private static final String OPEN_PDF_TEXT = "Open PDF";
    private static final String BEGIN_SIGN_TEXT = "Begin Sign";
    private static final String CANCEL_SIGN_TEXT = "Cancel Sign";

    public static PdfViewerMain INSTANCE = null; // Singleton reference
    private final SignerController signerController = new SignerController();

    private JPanel pdfPanel;
    private JScrollPane scrollPane;
    private Rectangle drawnRect = null;
    private Point startPoint = null;
    private JLabel activePageLabel = null;

    private PDDocument document;
    private File selectedPdfFile = null;
    private String pdfPassword = null;
    private int selectedPage = 0;
    private int[] pageCoords = new int[4];

    private JButton openBtn, beginSignBtn, settingsBtn;
    private JLabel pageInfoLabel;

    private boolean isSignModeEnabled = false;

    // --- Placeholder components ---
    private JPanel placeholderPanel;

    public PdfViewerMain() {
        super(APP_NAME);
        INSTANCE = this; // Assign singleton

        setIconImage(App.getAppIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height - 50;
        int screenWidth = screenSize.width;
        int frameWidth = Math.min(INITIAL_WIDTH, screenWidth);
        setSize(frameWidth, screenHeight);
        setPreferredSize(new Dimension(frameWidth, screenHeight));
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(createTopBar(), BorderLayout.NORTH);
        createScrollPane();
        initPlaceholderPanel();
        showPlaceholder(true); // Show placeholder initially
    }

    public void setWindowTitle(String title) {
        String generateTitle = Utils.generateTitle(APP_NAME, title, 50);
        setTitle(generateTitle);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        topBar.setBackground(FlatUIUtils.getUIColor("Panel.background", Color.WHITE));

        openBtn = createButton(OPEN_PDF_TEXT, new Color(0x007BFF));
        openBtn.addActionListener(e -> openPdf());

        beginSignBtn = createButton(BEGIN_SIGN_TEXT, new Color(0x28A745));
        beginSignBtn.setVisible(false);
        beginSignBtn.addActionListener(e -> toggleSignMode());

        settingsBtn = createButton("Settings", new Color(0x6C757D));
        settingsBtn.addActionListener(e -> new SettingsDialog(this).setVisible(true));

        pageInfoLabel = new JLabel("");
        pageInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(pageInfoLabel);
        centerPanel.add(beginSignBtn);

        topBar.add(createPanelWithFlow(openBtn, FlowLayout.LEFT), BorderLayout.WEST);
        topBar.add(centerPanel, BorderLayout.CENTER);
        topBar.add(createPanelWithFlow(settingsBtn, FlowLayout.RIGHT), BorderLayout.EAST);

        return topBar;
    }

    private void createScrollPane() {
        pdfPanel = new JPanel();
        pdfPanel.setLayout(new BoxLayout(pdfPanel, BoxLayout.Y_AXIS));
        pdfPanel.setBackground(Color.DARK_GRAY);

        // Wrapper panel to center pdfPanel horizontally
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        wrapper.setBackground(new Color(35, 35, 35));
        wrapper.add(pdfPanel);

        scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> updateCurrentPageBasedOnScroll());

        pdfPanel.setFocusable(true);
        pdfPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && isSignModeEnabled) {
                    resetSignMode();
                }
            }
        });

        add(scrollPane, BorderLayout.CENTER);
    }

    private void initPlaceholderPanel() {
        placeholderPanel = new JPanel();
        placeholderPanel.setLayout(new BoxLayout(placeholderPanel, BoxLayout.Y_AXIS));
        placeholderPanel.setBackground(new Color(35, 35, 35));

        JLabel placeholderLabel = new JLabel("Please Open Document PDF to sign");
        placeholderLabel.setForeground(Color.LIGHT_GRAY);
        placeholderLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton placeholderOpenBtn = createButton("Open PDF", new Color(0x007BFF));
        placeholderOpenBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeholderOpenBtn.addActionListener(e -> openPdf());

        placeholderPanel.add(Box.createVerticalGlue());
        placeholderPanel.add(placeholderLabel);
        placeholderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        placeholderPanel.add(placeholderOpenBtn);
        placeholderPanel.add(Box.createVerticalGlue());
    }

    private void showPlaceholder(boolean show) {
        scrollPane.setViewportView(show ? placeholderPanel : pdfPanel);
    }

    private JPanel createPanelWithFlow(JComponent component, int alignment) {
        JPanel panel = new JPanel(new FlowLayout(alignment));
        panel.setOpaque(false);
        panel.add(component);
        return panel;
    }

    private JButton createButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setMargin(new Insets(5, 10, 5, 10));
        if (bg != null) button.setBackground(bg);
        button.setForeground(Color.WHITE);
        return button;
    }

    private void toggleSignMode() {
        isSignModeEnabled = !isSignModeEnabled;
        updateSignModeUI();
        if (isSignModeEnabled) {
            String message =
                    "<html><body style='width:340px; font-family:Segoe UI, sans-serif; font-size:12px; " +
                            "line-height:1.5; color:#b6b6b6;'>" +

                            "<div style='margin-bottom:6px; color:#b7b7b7;'><b>Place your signature:</b></div>" +

                            "<ul style='margin:0; padding-left:20px; list-style-type:disc; list-style-position:outside;'>" +
                            "<li>Click where you want to sign</li>" +
                            "<li>Drag to set the area</li>" +
                            "<li>Release to confirm</li>" +
                            "</ul>" +
                            "</body></html>";

            DialogUtils.showHtmlMessageWithCheckbox(
                    this,
                    "Guide for Signing PDF",
                    message,
                    "showSignModeMessage"
            );
        }
    }



    private void updateSignModeUI() {
        beginSignBtn.setText(isSignModeEnabled ? CANCEL_SIGN_TEXT : BEGIN_SIGN_TEXT);
        openBtn.setEnabled(!isSignModeEnabled);
        settingsBtn.setEnabled(!isSignModeEnabled);
        applyCursorRecursively(pdfPanel, isSignModeEnabled ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
    }

    private void applyCursorRecursively(Component component, Cursor cursor) {
        component.setCursor(cursor);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyCursorRecursively(child, cursor);
            }
        }
    }

    private void setLoadingState(boolean isLoading) {
        setCursor(Cursor.getPredefinedCursor(isLoading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        setButtonsEnabled(!isLoading);
        openBtn.setText(isLoading ? "Opening PDF..." : OPEN_PDF_TEXT);
    }

    private void setButtonsEnabled(boolean enabled) {
        openBtn.setEnabled(enabled);
        settingsBtn.setEnabled(enabled);
        beginSignBtn.setEnabled(enabled);
    }

    private void openPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPdfFile = chooser.getSelectedFile();
            loadAndRenderPdf(selectedPdfFile);
        }
    }

    public void renderPdfFromPath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            DialogUtils.showError(this, "Error", "File not found: " + filePath);
            return;
        }
        selectedPdfFile = file;
        loadAndRenderPdf(file);
    }

    private void loadAndRenderPdf(File file) {
        setLoadingState(true);
        SwingUtilities.invokeLater(() -> {
            boolean success = renderPdf(file);
            updateUIAfterRender(success, file);
        });
    }

    private void updateUIAfterRender(boolean success, File file) {
        setLoadingState(false);
        if (success) {
            setWindowTitle(file.getAbsolutePath());
            beginSignBtn.setVisible(true);
            pageInfoLabel.setText("Page: 1/" + document.getNumberOfPages());
        } else {
            selectedPdfFile = null;
            beginSignBtn.setVisible(false);
            pageInfoLabel.setText("");
        }
        showPlaceholder(!success); // Show placeholder if render failed
        resetSignMode();
    }

    public boolean renderPdf(File file) {
        pdfPanel.removeAll();
        try {
            closeCurrentPdf();
            document = tryLoadDocument(file);
            if (document == null) return false;
            if (document.isEncrypted()) document.setAllSecurityToBeRemoved(true);

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, RENDER_DPI);

                // Wrap each page in a panel to center horizontally
                JPanel pageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                pageWrapper.setOpaque(false);

                JLabel pageLabel = new JLabel(new ImageIcon(image));
                pageLabel.setBorder(BorderFactory.createEmptyBorder(DEFAULT_RENDERER_PADDING, DEFAULT_RENDERER_PADDING, DEFAULT_RENDERER_PADDING, DEFAULT_RENDERER_PADDING));
                enableRectangleDrawing(pageLabel, i, RENDER_DPI / 72f);

                pageWrapper.add(pageLabel);
                pdfPanel.add(pageWrapper);
            }

            pdfPanel.revalidate();
            pdfPanel.repaint();
            return true;
        } catch (Exception ex) {
            if (ex instanceof UserCancelledPasswordEntryException) {
                log.info("User cancelled password entry.");
            } else {
                log.error("Error rendering PDF", ex);
                DialogUtils.showExceptionWithDetails(this, "Unable to Display PDF Preview, Please try again.", ex);
            }
        }
        return false;
    }

    private PDDocument tryLoadDocument(File file) throws Exception {
        int attempts = 0;
        final int maxAttempts = 3;

        try {
            return PDDocument.load(file);
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {

            PasswordDialog dialog = new PasswordDialog(
                    this,
                    null,
                    "PDF Document Password required",
                    "Password",
                    "Open Document",
                    "Cancel"
            );

            while (attempts < maxAttempts) {
                dialog.setVisible(true);

                if (!dialog.isConfirmed() || dialog.wasClosedByUser()) {
                    throw new UserCancelledPasswordEntryException("User cancelled password entry.");
                }

                try {
                    this.pdfPassword = dialog.getValue();
                    return PDDocument.load(file, this.pdfPassword);
                } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException ex) {
                    attempts++;
                    if (attempts < maxAttempts) {
                        int remaining = maxAttempts - attempts;
                        dialog.showInvalidMessage(
                                String.format("Invalid password — try again (<b>%d</b> left.)", remaining)
                        );
                    }
                }
            }

            DialogUtils.showError(this, "Access Denied", "Maximum password attempts reached. PDF loading cancelled.");
            throw new UserCancelledPasswordEntryException("Max password attempts exceeded.");
        }
    }

    private void updateCurrentPageBasedOnScroll() {
        if (document == null || pdfPanel.getComponentCount() == 0) {
            pageInfoLabel.setText("");
            return;
        }

        Rectangle viewportRect = scrollPane.getViewport().getViewRect();
        int totalPages = document.getNumberOfPages();

        for (int i = totalPages - 1; i >= 0; i--) {
            Component comp = pdfPanel.getComponent(i);
            Rectangle bounds = comp.getBounds();
            if (bounds.y + bounds.height - viewportRect.y <= viewportRect.height + 200) {
                pageInfoLabel.setText("Page: " + (i + 1) + "/" + totalPages);
                break;
            }
        }
    }

    private void enableRectangleDrawing(JLabel pageLabel, int pageIndex, float scale) {
        pageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            private Point localStartPoint = null;
            private Rectangle localDrawnRect = null;

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!isSignModeEnabled) return;

                beginSignBtn.setEnabled(false);
                localStartPoint = e.getPoint();
                localDrawnRect = new Rectangle();
                startPoint = localStartPoint;
                drawnRect = localDrawnRect;
                activePageLabel = pageLabel;
                selectedPage = pageIndex;
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (!isSignModeEnabled || localDrawnRect == null || localStartPoint == null || activePageLabel != pageLabel)
                    return;

                applyCursorRecursively(pdfPanel, Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                SwingUtilities.invokeLater(() -> {
                    try {
                        int[] coords = convertToItextRectangle(
                                e.getX(), e.getY(),
                                localStartPoint.x, localStartPoint.y,
                                pageLabel.getIcon().getIconHeight(),
                                scale
                        );

                        if (coords[2] - coords[0] <= 30 || coords[3] - coords[1] <= 10) {
                            DialogUtils.showInfo(PdfViewerMain.this, "", "Draw a larger rectangle to sign.");
                            drawnRect = null;
                            pageLabel.repaint();
                            return;
                        }

                        pageCoords = coords;

                        signerController.setSelectedFile(selectedPdfFile);
                        signerController.setPdfPassword(pdfPassword);
                        signerController.setPageNumber(selectedPage + 1);
                        signerController.setCoordinates(pageCoords);

                        signerController.startSigningService();

                    } catch (UserCancelledPasswordEntryException ex) {
                        log.info("User cancelled PDF password entry.");
                    } catch (IncorrectPINException ex) {
                        DialogUtils.showWarning(PdfViewerMain.INSTANCE, "Incorrect PIN", ex.getMessage());
                    } catch (Exception ex) {
                        log.error("Error signing PDF", ex);
                        DialogUtils.showExceptionWithDetails(PdfViewerMain.INSTANCE, "Signing Failed", ex);
                    } finally {
                        resetSignMode();
                    }
                });
            }
        });

        pageLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (!isSignModeEnabled || drawnRect == null || startPoint == null || activePageLabel != pageLabel)
                    return;

                int x = Math.min(startPoint.x, e.getX());
                int y = Math.min(startPoint.y, e.getY());
                int width = Math.abs(startPoint.x - e.getX());
                int height = Math.abs(startPoint.y - e.getY());
                drawnRect.setBounds(x, y, width, height);
                pageLabel.repaint();
            }
        });

        pageLabel.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);
                if (drawnRect != null && isSignModeEnabled && pageLabel == activePageLabel) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(60, 141, 188, 100));
                    g2.fill(drawnRect);
                    g2.setColor(new Color(60, 141, 188));
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(drawnRect);
                }
            }
        });
    }

    private int[] convertToItextRectangle(int endX, int endY, int startX, int startY, int imageHeight, float scale) {
        startX -= DEFAULT_RENDERER_PADDING;
        endX -= DEFAULT_RENDERER_PADDING;
        startY -= DEFAULT_RENDERER_PADDING;
        endY -= DEFAULT_RENDERER_PADDING;

        int x = Math.min(startX, endX);
        int y = Math.min(startY, endY);
        int width = Math.abs(startX - endX);
        int height = Math.abs(startY - endY);

        int llx = Math.round(x / scale);
        int lly = Math.round((imageHeight - y - height) / scale);
        int urx = Math.round((x + width) / scale);
        int ury = Math.round((imageHeight - y) / scale);

        llx = Math.max(0, llx);
        lly = Math.max(0, lly);
        urx = Math.max(llx, urx);
        ury = Math.max(lly, ury);

        return new int[]{llx, lly, urx, ury};
    }

    public void closeCurrentPdf() {
        try {
            if (document != null) {
                document.close();
            }
        } catch (Exception e) {
            log.error("Failed to close the current PDF document", e);
            DialogUtils.showError(
                    this,
                    "Unable to Close PDF",
                    "An unexpected error occurred while closing the PDF. Please try again."
            );
            System.exit(1);
        } finally {
            document = null;
            pdfPanel.removeAll();
            pdfPanel.revalidate();
            pdfPanel.repaint();
            pageInfoLabel.setText("");
            beginSignBtn.setVisible(false);
            showPlaceholder(true); // Show placeholder when PDF is closed
        }
    }

    private void resetSignMode() {
        drawnRect = null;
        activePageLabel = null;
        startPoint = null;
        isSignModeEnabled = false;
        beginSignBtn.setText(BEGIN_SIGN_TEXT);
        setButtonsEnabled(true);
        applyCursorRecursively(pdfPanel, Cursor.getDefaultCursor());
        pdfPanel.repaint();
    }
}

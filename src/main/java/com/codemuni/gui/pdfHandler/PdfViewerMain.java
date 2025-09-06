package com.codemuni.gui.pdfHandler;

import com.codemuni.App;
import com.codemuni.controller.SignerController;
import com.codemuni.gui.DialogUtils;
import com.codemuni.gui.settings.SettingsDialog;
import com.codemuni.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import static com.codemuni.utils.AppConstants.APP_NAME;

/**
 * Responsibilities:
 * - Window frame & layout
 * - Orchestrates top bar, scroll pane, renderer, and sign controller
 * - File open & preferences (last directory)
 * - Title updates & placeholder toggle
 */
public class PdfViewerMain extends JFrame {
    private static final Log log = LogFactory.getLog(PdfViewerMain.class);

    // Layout / sizing
    private static final int INITIAL_WIDTH = 950;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 400;

    // Preferences
    private static final Preferences prefs = Preferences.userNodeForPackage(PdfViewerMain.class);
    private static final String LAST_DIR_KEY = "lastPdfDir";

    // Singleton (if you still want it)
    public static PdfViewerMain INSTANCE = null;

    // Collaborators
    private final TopBarPanel topBar;
    private final PdfScrollPane pdfScrollPane;
    private final PlaceholderPanel placeholderPanel;
    private final PdfRendererService pdfRendererService;
    private final SignModeController signModeController;
    private final SignerController signerController = new SignerController();

    // State
    private File selectedPdfFile = null;
    private String pdfPassword = null;

    public PdfViewerMain() {
        super(APP_NAME);
        INSTANCE = this;

        setIconImage(App.getAppIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int frameWidth = Math.min(INITIAL_WIDTH, screen.width);
        int frameHeight = screen.height - 50;
        setSize(frameWidth, frameHeight);
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(null);

        // Services
        pdfRendererService = new PdfRendererService(this);
        signModeController = new SignModeController(
                PdfViewerMain.INSTANCE,
                pdfRendererService,
                signerController,
                this::onSignStart,
                this::onSignDone
        );

        // UI
        topBar = new TopBarPanel(
                this::openPdf,
                () -> new SettingsDialog(this).setVisible(true),
                signModeController::toggleSignMode
        );
        pdfScrollPane = new PdfScrollPane(
                pdfRendererService,
                topBar::setPageInfoText // callback to update page label
        );
        placeholderPanel = new PlaceholderPanel(this::openPdf);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(pdfScrollPane, BorderLayout.CENTER);

        showPlaceholder(true);
        enableDragAndDrop(placeholderPanel);
        enableDragAndDrop(pdfScrollPane);
    }

    /* --------------------------
       Public helpers / API
     --------------------------- */

    public void setWindowTitle(String titlePath) {
        String generated = Utils.truncateText(APP_NAME, titlePath, 70);
        setTitle(generated);
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

    /* --------------------------
       Internal wiring
     --------------------------- */

    private void showPlaceholder(boolean show) {
        if (show) {
            pdfScrollPane.setViewportView(placeholderPanel);
            topBar.setSignButtonVisible(false);
            topBar.setPageInfoText("");
        } else {
            pdfScrollPane.setViewportView(pdfScrollPane.getPdfPanel());
            topBar.setSignButtonVisible(true);
        }
        signModeController.resetSignModeUI();
    }

    private void setLoadingState(boolean loading) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        topBar.setLoading(loading);
    }

    private void openPdf() {
        JFileChooser chooser = new JFileChooser();
        String lastDir = prefs.get(LAST_DIR_KEY, null);
        if (lastDir != null) {
            chooser.setCurrentDirectory(new File(lastDir));
        }
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPdfFile = chooser.getSelectedFile();

            File parentDir = selectedPdfFile.getParentFile();
            if (parentDir != null) {
                prefs.put(LAST_DIR_KEY, parentDir.getAbsolutePath());
            }
            loadAndRenderPdf(selectedPdfFile);
        }
    }

    private void loadAndRenderPdf(File file) {
        setLoadingState(true);
        SwingUtilities.invokeLater(() -> {
            boolean ok = pdfRendererService.render(file); // handles password internally
            setLoadingState(false);
            if (ok) {
                setWindowTitle(file.getAbsolutePath());
                topBar.setSignButtonVisible(true);
                topBar.setPageInfoText("Page: 1/" + pdfRendererService.getPageCountSafe());
                showPlaceholder(false);
            } else {
                selectedPdfFile = null;
                topBar.setSignButtonVisible(false);
                topBar.setPageInfoText("");
                showPlaceholder(true);
            }
            signModeController.resetSignModeUI();
        });
    }

    private void enableDragAndDrop(JComponent component) {
        new DropTarget(component, DnDConstants.ACTION_COPY, new DropTargetListener() {

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    component.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                component.setBorder(null);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                component.setBorder(null);
                try {
                    Transferable tr = dtde.getTransferable();
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        List<File> files = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : files) {
                            if (file.getName().toLowerCase().endsWith(".pdf")) {
                                selectedPdfFile = file;
                                loadAndRenderPdf(file);
                                break; // only handle the first PDF
                            }
                        }
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception ex) {
                    dtde.dropComplete(false);
                    log.error("Drag-and-drop failed", ex);
                }
            }
        }, true, null);
    }

    /* --------------------------
       Sign mode lifecycle hooks
     --------------------------- */

    private void onSignStart() {
        // Disable open/settings while in sign mode
        topBar.setInteractiveEnabled(false);
    }

    private void onSignDone() {
        // Re-enable controls after signing flow completes or cancels
        topBar.setInteractiveEnabled(true);
    }

    public String getPdfPassword() {
        return pdfPassword;
    }

    public void setPdfPassword(String pdfPassword) {
        this.pdfPassword = pdfPassword;
    }
}

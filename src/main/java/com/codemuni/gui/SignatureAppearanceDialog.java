package com.codemuni.gui;

import com.codemuni.App;
import com.codemuni.core.keyStoresProvider.X509SubjectUtils;
import com.codemuni.core.signer.AppearanceOptions;
import com.codemuni.model.CertificationLevel;
import com.codemuni.model.RenderingMode;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import static com.codemuni.core.keyStoresProvider.X509SubjectUtils.getCommonName;
import static com.codemuni.core.keyStoresProvider.X509SubjectUtils.getOrganization;

public class SignatureAppearanceDialog extends JDialog {

    private static final Log log = LogFactory.getLog(SignatureAppearanceDialog.class);
    private final Frame parent;
    private X509Certificate certificate;
    private JTextField reasonField;
    private JTextField locationField;
    private JTextField customTextField;
    private JCheckBox ltvCheckbox, timestampCheckbox, greenTickCheckbox, includeCompanyCheckbox, includeEntireSubjectDNCheckbox;
    private JComboBox<String> renderingModeCombo, certLevelCombo;
    private JButton chooseImageButton;
    private File selectedImageFile;
    private JPanel previewPanel;
    private AppearanceOptions appearanceOptions;

    public SignatureAppearanceDialog(Frame parent) {
        super(parent, "Signature Appearance Settings", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        this.parent = parent;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public void showAppearanceConfigPrompt() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        previewPanel = new JPanel(new BorderLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Live Preview");
        titledBorder.setTitleColor(Color.LIGHT_GRAY);
        titledBorder.setTitleFont(new Font("SansSerif", Font.PLAIN, 12));
        previewPanel.setBorder(titledBorder);
        previewPanel.setBackground(new Color(106, 106, 106));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");
        getRootPane().setDefaultButton(okButton);
        cancelButton.addActionListener(e -> dispose());
        okButton.addActionListener(this::onSubmit);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton(okButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(previewPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setupListeners();
        updatePreview();

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Create document filters for each field
        DocumentFilter reasonFilter = TextFieldValidators.createAlphanumericFilter(25);
        DocumentFilter locationFilter = TextFieldValidators.createAlphanumericFilter(25);
        DocumentFilter customTextFilter = TextFieldValidators.createAlphanumericFilter(60);

        // Rendering Mode
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Rendering Mode:"), gbc);
        gbc.gridy++;
        renderingModeCombo = new JComboBox<>(
                Arrays.stream(RenderingMode.values()).map(RenderingMode::getLabel).toArray(String[]::new)
        );
        formPanel.add(renderingModeCombo, gbc);

        // Signature Permissions
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Signature Permissions:"), gbc);
        gbc.gridy++;
        certLevelCombo = new JComboBox<>(
                Arrays.stream(CertificationLevel.values()).map(CertificationLevel::getLabel).toArray(String[]::new)
        );
        formPanel.add(certLevelCombo, gbc);

        // Reason
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Reason (max 25 chars):"), gbc);
        gbc.gridy++;
        reasonField = new JTextField(15);
        ((AbstractDocument) reasonField.getDocument()).setDocumentFilter(reasonFilter);
        formPanel.add(reasonField, gbc);

        // Location
        gbc.gridx = 1;
        gbc.gridy -= 1;
        formPanel.add(new JLabel("Location (max 25 chars):"), gbc);
        gbc.gridy++;
        locationField = new JTextField(15);
        ((AbstractDocument) locationField.getDocument()).setDocumentFilter(locationFilter);
        formPanel.add(locationField, gbc);

        // Custom Text
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(new JLabel("Custom Text (max 60 chars):"), gbc);
        gbc.gridy++;
        customTextField = new JTextField(30);
        ((AbstractDocument) customTextField.getDocument()).setDocumentFilter(customTextFilter);
        formPanel.add(customTextField, gbc);
        gbc.gridwidth = 1;

        // Options Checkboxes
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel checkboxPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        checkboxPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        ltvCheckbox = new JCheckBox("LTV");
        timestampCheckbox = new JCheckBox("Timestamp");
        greenTickCheckbox = new JCheckBox("Green Tick");
        includeCompanyCheckbox = new JCheckBox("Include Org Name");
        includeEntireSubjectDNCheckbox = new JCheckBox("Include Subject DN");

        boolean isPersonalCert = getOrganization(certificate).equalsIgnoreCase("Personal");
        includeCompanyCheckbox.setEnabled(!isPersonalCert);
        includeCompanyCheckbox.setToolTipText(isPersonalCert
                ? "Organization name not available for personal certificates."
                : "Include organization name from certificate.");
        includeEntireSubjectDNCheckbox.setToolTipText("Include full Subject Distinguished Name (DN).");

        checkboxPanel.add(ltvCheckbox);
        checkboxPanel.add(timestampCheckbox);
        checkboxPanel.add(includeCompanyCheckbox);
        checkboxPanel.add(includeEntireSubjectDNCheckbox);
        checkboxPanel.add(greenTickCheckbox);

        formPanel.add(checkboxPanel, gbc);
        gbc.gridwidth = 1;

        // Graphic Image Selection
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Graphic Image (optional):"), gbc);

        gbc.gridx = 1;
        JPanel imageButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chooseImageButton = new JButton("Choose Graphic Image");
        chooseImageButton.setEnabled(false);
        imageButtonPanel.add(chooseImageButton);
        formPanel.add(imageButtonPanel, gbc);

        return formPanel;
    }


    private void setupListeners() {
        chooseImageButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = chooser.getSelectedFile();
                updatePreview();
            }
        });

        renderingModeCombo.addItemListener(e -> {
            boolean isGraphic = "Name and Graphic".equals(renderingModeCombo.getSelectedItem());
            chooseImageButton.setEnabled(isGraphic);
            updatePreview();
        });

        greenTickCheckbox.addActionListener(e -> updatePreview());
        includeCompanyCheckbox.addActionListener(e -> updatePreview());
        includeEntireSubjectDNCheckbox.addActionListener(e -> {
            boolean selected = includeEntireSubjectDNCheckbox.isSelected();
            includeCompanyCheckbox.setEnabled(!selected && !getOrganization(certificate).equalsIgnoreCase("Personal"));
            updatePreview();
        });

        DocumentListener docUpdate = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            public void changedUpdate(DocumentEvent e) {
                updatePreview();
            }
        };
        reasonField.getDocument().addDocumentListener(docUpdate);
        locationField.getDocument().addDocumentListener(docUpdate);
        customTextField.getDocument().addDocumentListener(docUpdate);
    }

    private void updatePreview() {
        previewPanel.removeAll();

        String customText = customTextField.getText().trim();
        String reason = reasonField.getText().trim();
        String location = locationField.getText().trim();
        String renderingMode = (String) renderingModeCombo.getSelectedItem();
        String time = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss xxx"));

        JPanel overlayContainer = new JPanel();
        overlayContainer.setLayout(new OverlayLayout(overlayContainer));
        overlayContainer.setPreferredSize(new Dimension(400, 200));
        overlayContainer.setOpaque(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.setOpaque(false);

        JLabel leftLabel = new JLabel();
        leftLabel.setVerticalAlignment(SwingConstants.TOP);
        leftLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding

        if ("Name and Description".equals(renderingMode)) {
            leftLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            leftLabel.setText("<html><div style='font-weight:bold; color: black;'>" + getCommonName(certificate).replace(" ", "<br>") + "</div></html>");
        } else if ("Name and Graphic".equals(renderingMode) && selectedImageFile != null && selectedImageFile.exists()) {
            ImageIcon icon = new ImageIcon(selectedImageFile.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(80, 50, Image.SCALE_SMOOTH);
            leftLabel.setIcon(new ImageIcon(scaled));
        }

        // Prepare content for font sizing
        StringBuilder previewText = new StringBuilder();
        if (includeEntireSubjectDNCheckbox.isSelected()) {
            previewText.append(X509SubjectUtils.getFullSubjectDN(certificate)).append("\n");
        } else {
            log.debug("Subject DN: " + X509SubjectUtils.getFullSubjectDN(certificate));
            previewText.append("Signed by: ").append(getCommonName(certificate)).append("\n");
            String organization = getOrganization(certificate);
            if (includeCompanyCheckbox.isSelected() && organization != null && !organization.trim().isEmpty()) {
                previewText.append("ORG: ").append(organization).append("\n");
            }

        }
        previewText.append("Date: ").append(time).append("\n");
        if (!reason.isEmpty()) previewText.append("Reason: ").append(reason).append("\n");
        if (!location.isEmpty()) previewText.append("Location: ").append(location).append("\n");
        if (!customText.isEmpty()) previewText.append(customText).append("\n");

        int fontSize = computeFontSizeForPreview(previewText.toString(), previewPanel.getWidth());

        StringBuilder rightHtml = new StringBuilder("<html><div style='font-family:sans-serif;color:black;'>");
        for (String line : previewText.toString().split("\n")) {
            rightHtml.append("<div style='font-size:").append(fontSize).append("px;'>").append(line).append("</div>");
        }
        rightHtml.append("</div></html>");

        JLabel rightLabel = new JLabel(rightHtml.toString());
        rightLabel.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        rightLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding
        rightLabel.setVerticalAlignment(SwingConstants.TOP);

        contentPanel.add(leftLabel);
        contentPanel.add(rightLabel);
        contentPanel.add(Box.createHorizontalGlue());
        overlayContainer.add(contentPanel);

        if (greenTickCheckbox.isSelected()) {
            try {
                ImageIcon tickIcon = new ImageIcon(Objects.requireNonNull(App.class.getResource("/icons/green_tick.png")));
                Image scaledTick = tickIcon.getImage().getScaledInstance(140, 120, Image.SCALE_SMOOTH);
                JLabel tickLabel = new JLabel(new ImageIcon(scaledTick));
                tickLabel.setAlignmentX(0.5f);
                tickLabel.setAlignmentY(0.5f);
                overlayContainer.add(tickLabel);
            } catch (Exception ex) {
                log.error("Failed to load green tick icon", ex);
            }
        }

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        centerWrapper.setBackground(new Color(240, 240, 240));
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6)); // Add 6px padding
        centerWrapper.add(overlayContainer);

        previewPanel.setLayout(new BorderLayout());
        previewPanel.add(centerWrapper, BorderLayout.CENTER);
        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private int computeFontSizeForPreview(String text, int panelWidth) {
        int maxFontSize = 16;
        int minFontSize = 10;
        int length = text.length();
        int size = maxFontSize - (length / 20);
        return Math.max(minFontSize, size);
    }

    private void onSubmit(ActionEvent e) {
        if ("Name and Graphic".equals(renderingModeCombo.getSelectedItem()) && selectedImageFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a graphic image for the signature.", "Missing Image", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String renderingLabel = (String) renderingModeCombo.getSelectedItem();
        String certLabel = (String) certLevelCombo.getSelectedItem();
        RenderingMode selectedRendering = RenderingMode.fromLabel(renderingLabel);
        CertificationLevel selectedCertLevel = CertificationLevel.fromLabel(certLabel);

        appearanceOptions = new AppearanceOptions();
        appearanceOptions.setGraphicRendering(selectedRendering == RenderingMode.NAME_AND_GRAPHIC);

        int certLevel = PdfSignatureAppearance.NOT_CERTIFIED;
        switch (Objects.requireNonNull(selectedCertLevel, "Document certification level is null")) {
            case NO_CHANGES_ALLOWED:
                certLevel = PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED;
                break;
            case FORM_FILLING:
                certLevel = PdfSignatureAppearance.CERTIFIED_FORM_FILLING;
                break;
            case FORM_FILLING_AND_ANNOTATION:
                certLevel = PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS;
                break;
        }

        appearanceOptions.setIncludeCompany(includeCompanyCheckbox.isSelected());
        appearanceOptions.setIncludeEntireSubject(includeEntireSubjectDNCheckbox.isSelected());
        appearanceOptions.setCertificationLevel(certLevel);
        appearanceOptions.setReason(reasonField.getText().trim());
        appearanceOptions.setLocation(locationField.getText().trim());
        appearanceOptions.setCustomText(customTextField.getText().trim());
        appearanceOptions.setLtvEnabled(ltvCheckbox.isSelected());
        appearanceOptions.setTimestampEnabled(timestampCheckbox.isSelected());
        appearanceOptions.setGreenTickEnabled(greenTickCheckbox.isSelected());
        appearanceOptions.setGraphicImagePath(
                selectedRendering == RenderingMode.NAME_AND_GRAPHIC && selectedImageFile != null
                        ? selectedImageFile.getAbsolutePath()
                        : null
        );
        dispose();
    }

    public AppearanceOptions getAppearanceOptions() {
        return appearanceOptions;
    }
}

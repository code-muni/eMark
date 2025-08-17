package com.codemuni.core.signer;

import com.codemuni.core.keyStoresProvider.KeyStoreProvider;
import com.codemuni.exceptions.NotADigitalSignatureException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfTemplate;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.codemuni.core.keyStoresProvider.X509SubjectUtils.*;
import static com.codemuni.utils.AppConstants.APP_AUTHOR;
import static com.codemuni.utils.AppConstants.APP_NAME;

public class SignatureAppearanceHandler {

    private final KeyStoreProvider keyStoreProvider;
    private final AppearanceOptions options;

    public SignatureAppearanceHandler(KeyStoreProvider keyStoreProvider, AppearanceOptions options) {
        this.keyStoreProvider = keyStoreProvider;
        this.options = options;
    }

    public void configureAppearance(PdfSignatureAppearance appearance) throws CertificateExpiredException, KeyStoreException, NotADigitalSignatureException, DocumentException, IOException {
        appearance.setSignatureCreator(APP_AUTHOR + "( " + APP_NAME + " )");


        setVisibleSignature(appearance);
        setRenderingMode(appearance);
        setCertificationAndInfo(appearance);
        setLayer2Text(appearance);
        if (options.getWatermarkImage() != null) applyWatermark(appearance);
    }

    private void setVisibleSignature(PdfSignatureAppearance appearance) {
        int[] coord = options.getCoordinates();
        if (coord != null && coord.length == 4) {
            Rectangle rect = new Rectangle(coord[0], coord[1], coord[2], coord[3]);
            appearance.setVisibleSignature(rect, options.getPageNumber(), generateFieldName(options.getPageNumber()));
        }
    }

    private void setRenderingMode(PdfSignatureAppearance appearance) {
        if (options.isGraphicRendering()) {
            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION);
            try {
                Image img = Image.getInstance(options.getGraphicImagePath());
                appearance.setSignatureGraphic(img);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load signature graphic image.", e);
            }
        } else {
            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION);
        }
        appearance.setAcro6Layers(!options.isGreenTickEnabled());
    }

    private void setCertificationAndInfo(PdfSignatureAppearance appearance) {
        appearance.setCertificationLevel(options.getCertificationLevel());

        if (isNotEmpty(options.getReason())) appearance.setReason(options.getReason().trim());
        if (isNotEmpty(options.getLocation())) appearance.setLocation(options.getLocation().trim());
    }

    private void setLayer2Text(PdfSignatureAppearance appearance) throws KeyStoreException, CertificateExpiredException, NotADigitalSignatureException, DocumentException, IOException {
        X509Certificate cert = (X509Certificate) keyStoreProvider.getCertificateChain()[0];
        String layerText = buildLayerText(cert, options);

        appearance.setLayer2Text(layerText);
    }

    private String buildLayerText(X509Certificate cert, AppearanceOptions options) {
        StringBuilder sb = new StringBuilder();

        if (options.isIncludeEntireSubject()) {
            sb.append(getFullSubjectDN(cert)).append("\n\n");
        } else {
            sb.append("Signed by: ").append(getCommonName(cert)).append("\n");
            if (options.isIncludeCompany()) sb.append("ORG: ").append(getOrganization(cert)).append("\n");
        }

        String dateTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss z"));
        if (isNotEmpty(options.getReason())) sb.append("Reason: ").append(options.getReason().trim()).append("\n");
        if (isNotEmpty(options.getLocation()))
            sb.append("Location: ").append(options.getLocation().trim()).append("\n");
        if (isNotEmpty(options.getCustomText())) sb.append(options.getCustomText().trim()).append("\n");
        sb.append("Date: ").append(dateTime);

        return sb.toString();
    }

    private void applyWatermark(PdfSignatureAppearance appearance) {
        int[] coords = options.getCoordinates();
        float rectWidth = coords[2] - coords[0];
        float rectHeight = coords[3] - coords[1];

        options.getWatermarkImage().scaleToFit(rectWidth, rectHeight);

        float imageWidth = options.getWatermarkImage().getScaledWidth();
        float imageHeight = options.getWatermarkImage().getScaledHeight();

        float xOffset = (rectWidth - imageWidth) / 2;
        float yOffset = (rectHeight - imageHeight) / 2;
        options.getWatermarkImage().setAbsolutePosition(xOffset, yOffset);

        PdfTemplate background = appearance.getLayer(0); // Layer 0 = background
        PdfGState gState = new PdfGState();
        gState.setFillOpacity(0.20f); // 20% transparency

        background.saveState();
        background.setGState(gState);
        try {
            background.addImage(options.getWatermarkImage());
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to add watermark to signature appearance.", e);
        }
        background.restoreState();
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String generateFieldName(int pageNumber) {
        return String.format(APP_NAME + "__P_%d_%d", pageNumber, (int) (Math.random() * 900_000));
    }
}

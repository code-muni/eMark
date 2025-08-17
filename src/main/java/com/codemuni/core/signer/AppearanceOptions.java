package com.codemuni.core.signer;


import com.itextpdf.text.Image;

import java.util.Arrays;

public class AppearanceOptions {
    private boolean isGraphicRendering;
    private boolean includeEntireSubject;
    private String graphicImagePath;
    private int certificationLevel;
    private boolean includeCompany;
    private String reason;
    private String location;
    private String customText;
    private boolean ltvEnabled;
    private boolean timestampEnabled;
    private boolean greenTickEnabled;
    private int pageNumber;
    private int[] coordinates = {0, 0, 0, 0};

    private Image watermarkImage;

    public AppearanceOptions() {
    }

    public Image getWatermarkImage() {
        return watermarkImage;
    }

    public void setWatermarkImage(Image watermarkImage) {
        this.watermarkImage = watermarkImage;
    }

    // Getters and Setters
    public boolean isGraphicRendering() {
        return isGraphicRendering;
    }

    public void setGraphicRendering(boolean renderingMode) {
        this.isGraphicRendering = renderingMode;
    }

    public int getCertificationLevel() {
        return certificationLevel;
    }

    public void setCertificationLevel(int certificationLevel) {
        this.certificationLevel = certificationLevel;
    }


    public boolean isIncludeEntireSubject() {return includeEntireSubject;}
    public void setIncludeEntireSubject(boolean includeEntireSubject) {this.includeEntireSubject = includeEntireSubject;}
    public boolean isIncludeCompany() {return includeCompany;}
    public void setIncludeCompany(boolean includeCompany) {this.includeCompany = includeCompany;}

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCustomText() {
        return customText;
    }

    public void setCustomText(String customText) {
        this.customText = customText;
    }

    public boolean isLtvEnabled() {
        return ltvEnabled;
    }

    public void setLtvEnabled(boolean ltvEnabled) {
        this.ltvEnabled = ltvEnabled;
    }

    public boolean isTimestampEnabled() {
        return timestampEnabled;
    }

    public void setTimestampEnabled(boolean timestampEnabled) {
        this.timestampEnabled = timestampEnabled;
    }

    public boolean isGreenTickEnabled() {
        return greenTickEnabled;
    }

    public void setGreenTickEnabled(boolean greenTickEnabled) {
        this.greenTickEnabled = greenTickEnabled;
    }

    public String getGraphicImagePath() {
        return graphicImagePath;
    }

    public void setGraphicImagePath(String graphicImagePath) {
        this.graphicImagePath = graphicImagePath;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(int[] coordinates) {
        this.coordinates = coordinates;
    }

    public void setCoordinates(float pdfX, float pdfY, float pdfWidth, float pdfHeight) {
        int x = (int) pdfX;
        int y = (int) pdfY;
        int width = (int) pdfWidth;
        int height = (int) pdfHeight;
        coordinates[0] = x;
        coordinates[1] = y;
        coordinates[2] = width;
        coordinates[3] = height;
    }


    @Override
    public String toString() {
        return "SignatureAppearanceSettings{" +
                "isGraphicRendering='" + isGraphicRendering + '\'' +
                ", certificationLevel='" + certificationLevel + '\'' +
                ", reason='" + reason + '\'' +
                ", location='" + location + '\'' +
                ", customText='" + customText + '\'' +
                ", ltvEnabled=" + ltvEnabled +
                ", timestampEnabled=" + timestampEnabled +
                ", greenTickEnabled=" + greenTickEnabled +
                ", graphicImagePath='" + graphicImagePath + '\'' +
                ", pageNumber='" + pageNumber + '\'' +
                ", coordinates='" + Arrays.toString(coordinates) + '\'' +
                '}';
    }
}

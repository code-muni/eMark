package com.codemuni.model;

public enum CertificationLevel {
    NOT_CERTIFIED("Editable", "NOT_CERTIFIED"),
    NO_CHANGES_ALLOWED("Locked", "NO_CHANGES_ALLOWED"),
    FORM_FILLING("Forms Only", "FORM_FILLING"),
    FORM_FILLING_AND_ANNOTATION("Forms + Notes", "FORM_FILLING_AND_ANNOTATION");

    private final String label;
    private final String id;

    CertificationLevel(String label, String id) {
        this.label = label;
        this.id = id;
    }

    public static CertificationLevel fromLabel(String label) {
        for (CertificationLevel level : values()) {
            if (level.label.equals(label)) return level;
        }
        return null;
    }

    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }
}

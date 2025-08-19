package com.codemuni.model;

public enum RenderingMode {
    NAME_AND_DESCRIPTION("Name and Description", "NAME_AND_DESCRIPTION"),
    NAME_AND_GRAPHIC("Name and Graphic", "NAME_AND_GRAPHIC");

    private final String label;
    private final String id;

    RenderingMode(String label, String id) {
        this.label = label;
        this.id = id;
    }

    public static RenderingMode fromLabel(String label) {
        for (RenderingMode mode : values()) {
            if (mode.label.equals(label)) return mode;
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

package com.codemuni.gui.pdfHandler;

/**
 * Utility to convert drag rectangle (screen/image coords) to iText's PDF coords.
 * Keeps original behavior including padding and clamping.
 */
public final class SelectionUtils {
    private SelectionUtils() {
    }

    public static int[] convertToItextRectangle(
            int endX, int endY,
            int startX, int startY,
            int imageHeight,
            float scale,
            int padding
    ) {
        startX -= padding;
        endX -= padding;
        startY -= padding;
        endY -= padding;

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
}

package com.codemuni.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class TextFieldValidators {

    /**
     * Creates a DocumentFilter that restricts input to alphanumeric and common punctuation,
     * with a maximum length.
     */
    public static DocumentFilter createAlphanumericFilter(int maxLength) {
        return new DocumentFilter() {

            private boolean isValid(String text) {
                if (text == null || text.isEmpty()) return true;
                for (int i = 0; i < text.length(); i++) {
                    String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 .,!?()-:;'\"";
                    if (ALLOWED_CHARS.indexOf(text.charAt(i)) == -1) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;

                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + string +
                        currentText.substring(offset);

                if (newText.length() <= maxLength && isValid(string)) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text,
                                AttributeSet attrs) throws BadLocationException {
                if (text == null) return;

                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text +
                        currentText.substring(offset + length);

                if (newText.length() <= maxLength && isValid(text)) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        };
    }
}

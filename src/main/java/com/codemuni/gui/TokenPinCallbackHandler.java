package com.codemuni.gui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;

/**
 * A Swing-based PIN entry handler for PKCS#11 tokens.
 * Uses the reusable PasswordDialog for PIN entry.
 * Supports cancellation without throwing exceptions unnecessarily.
 */
public class TokenPinCallbackHandler implements CallbackHandler {

    private static final Log log = LogFactory.getLog(TokenPinCallbackHandler.class);
    private String labelText;
    private boolean cancelled = false; // Track cancellation state

    public TokenPinCallbackHandler(String labelText) {
        this.labelText = labelText != null ? labelText : "Enter your token PIN:";
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText != null ? labelText : "Enter your token PIN:";
    }

    /**
     * Whether the user cancelled the PIN prompt.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof PasswordCallback) {
                handlePasswordCallback((PasswordCallback) callback);
            } else {
                log.warn("Unsupported callback type: " + callback.getClass().getName());
                throw new UnsupportedCallbackException(callback, "Only PasswordCallback is supported.");
            }
        }
    }

    private void handlePasswordCallback(PasswordCallback passwordCallback) {
        cancelled = false; // reset state

        while (true) {
            char[] pin = promptUserForPin();

            if (pin == null) {
                log.info("PIN entry cancelled by user.");
                cancelled = true;
                return; // exit gracefully
            }

            if (pin.length == 0) {
                // Show an error inside the dialog in the next loop iteration
                JOptionPane.showMessageDialog(
                        null,
                        "PIN cannot be empty.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                continue; // retry
            }

            log.debug("PIN successfully entered (length: " + pin.length + ")");
            passwordCallback.setPassword(pin);
            clearInMemory(pin);
            return;
        }
    }

    /**
     * Displays the PIN entry dialog and returns the entered PIN, or null if cancelled.
     */
    private char[] promptUserForPin() {
        PasswordDialog dialog = new PasswordDialog(
                null,                                   // parent window
                "PKCS#11 PIN Required",                 // title
                labelText,                              // message
                "Token PIN",                            // placeholder
                "OK",                                   // okText
                "Cancel"                                // cancelText
        );

        dialog.setValidator(value -> value != null && !value.trim().isEmpty());
        dialog.setVisible(true); // Blocks until closed

        if (dialog.isConfirmed()) {
            return dialog.getValue().toCharArray();
        }
        return null; // cancelled
    }

    /**
     * Securely clears sensitive data from memory.
     */
    private void clearInMemory(char[] data) {
        if (data != null) {
            Arrays.fill(data, '\0');
        }
    }
}


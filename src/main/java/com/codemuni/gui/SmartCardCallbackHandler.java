package com.codemuni.gui;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * Custom CallbackHandler that shows a Swing dialog for PIN entry.
 */
public class SmartCardCallbackHandler implements CallbackHandler {

    private volatile String statusMessage;
    private boolean cancelled = false;
    private PasswordDialog dialog;

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (!(callback instanceof PasswordCallback)) {
                throw new UnsupportedCallbackException(callback,
                        "Unsupported Callback type: " + callback.getClass().getName());
            }

            PasswordCallback pc = (PasswordCallback) callback;
            PasswordDialog dialog = createPasswordDialog();

            if (dialog.isConfirmed()) {
                String value = dialog.getValue();
                if (value == null || value.trim().isEmpty()) {
                    throw new IOException("Empty PIN not allowed");
                }
                pc.setPassword(value.toCharArray());
            } else {
                cancelled = true; // user cancelled
            }
        }
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private PasswordDialog createPasswordDialog() {
        PasswordDialog dialog = new PasswordDialog(
                null,
                "Smartcard Authentication",
                buildMessage(),
                "Enter PIN",
                "Authenticate",
                "Cancel"
        );
        dialog.setValidator(value -> value != null && value.trim().length() >= 2);
        dialog.setVisible(true); // blocks until closed
        return dialog;
    }

    private String buildMessage() {
        return (statusMessage != null && !statusMessage.isEmpty())
                ? statusMessage
                : "Please enter your PIN:";
    }
}

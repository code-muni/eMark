package com.codemuni.utils;

import java.util.logging.*;

/**
 * A lightweight, reusable stdout logger for Java 8.
 * Provides simple static methods: info, warn, error, debug.
 * Uses java.util.logging (JUL) with custom formatting and stdout only.
 */
public class Logger {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Logger.class.getName());
    private static final Level DEFAULT_LEVEL = Level.ALL; // You can change default level

    static {
        // Ensure this logger doesn't inherit handlers from root
        LOGGER.setUseParentHandlers(false);

        // Remove existing handlers from root to avoid duplicates
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        // Create and configure ConsoleHandler
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(DEFAULT_LEVEL);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format(
                        "[%s] %s - %s%n",
                        record.getLevel(),
                        record.getLoggerName(),
                        record.getMessage()
                );
            }
        });

        // Add handler to this logger
        LOGGER.addHandler(handler);
        LOGGER.setLevel(DEFAULT_LEVEL);
    }

    // === Public Logging Methods ===

    public static void info(String message) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(message);
        }
    }

    public static void warn(String message) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.warning(message);
        }
    }

    public static void error(String message) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.severe(message);
        }
    }

    public static void debug(String message) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(message);
        }
    }

    public static void error(String message, Throwable throwable) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.log(Level.SEVERE, message, throwable);
        }
    }

    public static void debug(String message, Throwable throwable) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, message, throwable);
        }
    }

    // Optional: Get current log level
    public static Level getLevel() {
        return LOGGER.getLevel();
    }

    // Optional: Set log level dynamically
    public static void setLevel(Level level) {
        LOGGER.setLevel(level);
        for (Handler h : LOGGER.getHandlers()) {
            h.setLevel(level);
        }
    }
}
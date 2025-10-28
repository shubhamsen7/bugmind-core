package com.bugmind.core;

/**
 * Represents a structured log entry parsed from raw text.
 */
public class ParsedLog {
    private final String timestamp;
    private final String level;
    private final String message;
    private final String exceptionType;

    public ParsedLog(String timestamp, String level, String message, String exceptionType) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.exceptionType = exceptionType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + level + ": " + message +
               (exceptionType != null ? " (" + exceptionType + ")" : "");
    }
}

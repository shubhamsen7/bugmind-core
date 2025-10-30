package com.bugmind.core;

/**
 * Represents a structured log entry parsed from raw text.
 */
public class ParsedLog {

    private final String timestamp;
    private final String level;
    private String message; // ✅ remove final, so it can be updated
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

    /**
     * Allows updating the message content (e.g. after collapsing multi-line stack traces).
     * Trims whitespace and ensures null-safety.
     */
    public void setMessage(String newMessage) {
        this.message = (newMessage == null) ? "" : newMessage.trim();
    }

    @Override
    public String toString() {
        return "[" + (timestamp != null ? timestamp : "-") + "] "
                + (level != null ? level : "-") + ": "
                + (message != null ? message : "")
                + (exceptionType != null ? " (" + exceptionType + ")" : "");
    }
}

package com.bugmind.core;

/**
 * Simple data model representing parsed log details.
 */
public class ParsedLog {

    private final String timestamp;
    private final String level;
    private final String errorType;

    public ParsedLog(String timestamp, String level, String errorType) {
        this.timestamp = timestamp;
        this.level = level;
        this.errorType = errorType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getErrorType() {
        return errorType;
    }

    public boolean hasAnyField() {
        return timestamp != null || level != null || errorType != null;
    }

    @Override
    public String toString() {
        return String.format("[time=%s, level=%s, error=%s]",
                timestamp, level, errorType);
    }
}

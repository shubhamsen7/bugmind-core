package com.bugmind.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Represents a structured log entry parsed from raw text,
 * enhanced with timestamp normalization support.
 */
public class ParsedLog {

    private String timestamp;
    private final String level;
    private String message;
    private final String exceptionType;
    private LocalDateTime parsedTimestamp;

    private static final List<DateTimeFormatter> SUPPORTED_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
    );

    public ParsedLog(String timestamp, String level, String message, String exceptionType) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.exceptionType = exceptionType;
        this.parsedTimestamp = parseTimestampSafe(timestamp);
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

    public LocalDateTime getParsedTimestamp() {
        return parsedTimestamp;
    }

    public void setMessage(String newMessage) {
        this.message = (newMessage == null) ? "" : newMessage.trim();
    }

    public void setTimestamp(String newTimestamp) {
        this.timestamp = newTimestamp;
        this.parsedTimestamp = parseTimestampSafe(newTimestamp);
    }

    /**
     * Attempts to parse multiple known timestamp formats safely.
     * Returns null if all formats fail, allowing nullsLast sorting.
     */
    private LocalDateTime parseTimestampSafe(String ts) {
        if (ts == null || ts.isBlank()) return null;
        for (DateTimeFormatter fmt : SUPPORTED_FORMATS) {
            try {
                return LocalDateTime.parse(ts.trim(), fmt);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    @Override
    public String toString() {
        return "[" + (timestamp != null ? timestamp : "-") + "] "
                + (level != null ? level : "-") + ": "
                + (message != null ? message : "")
                + (exceptionType != null ? " (" + exceptionType + ")" : "");
    }
}

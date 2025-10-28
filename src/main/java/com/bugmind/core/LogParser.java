package com.bugmind.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced parser for structured log lines.
 * <p>
 * Supports multiple timestamp formats and gracefully skips malformed inputs.
 */
public class LogParser {

    // Original strict pattern
    private static final Pattern STRICT_PATTERN =
        Pattern.compile("\\[(.*?)\\]\\s+(INFO|WARN|ERROR|DEBUG)\\s+-\\s+(.*)");

    // More flexible pattern for ISO timestamps or extra spaces
    private static final Pattern FLEXIBLE_PATTERN =
        Pattern.compile("\\[([0-9T:\\-\\s]+Z?)\\]\\s*(INFO|WARN|ERROR|DEBUG)\\s*-\\s*(.*)");

    private static final Pattern EXCEPTION_PATTERN =
        Pattern.compile("([A-Za-z0-9_$.]+Exception)");

    // Common timestamp formats encountered in real-world logs
    private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[]{
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    };

    /**
     * Parses a single log line into a structured {@link ParsedLog}.
     * Supports flexible timestamps and whitespace handling.
     *
     * @param line Raw log line text
     * @return ParsedLog if successfully matched, otherwise {@code null}
     */
    public ParsedLog parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        // Try strict pattern first, then flexible
        Matcher matcher = STRICT_PATTERN.matcher(line);
        if (!matcher.find()) {
            matcher = FLEXIBLE_PATTERN.matcher(line);
            if (!matcher.find()) {
                return null;
            }
        }

        String timestamp = normalizeTimestamp(matcher.group(1));
        String level = safeTrim(matcher.group(2));
        String message = safeTrim(matcher.group(3));

        String exception = extractException(message);
        return new ParsedLog(timestamp, level, message, exception);
    }

    /** Safely trims a string or returns an empty string. */
    private String safeTrim(String input) {
        return input == null ? "" : input.trim();
    }

    /** Extracts exception name from message text if present. */
    private String extractException(String message) {
        if (message == null) return null;
        Matcher m = EXCEPTION_PATTERN.matcher(message);
        return m.find() ? m.group(1) : null;
    }

    /** Normalizes timestamp text into consistent human-readable form. */
    private String normalizeTimestamp(String ts) {
        if (ts == null || ts.isBlank()) return "";
        String candidate = ts.replace("T", " ").trim();
        for (DateTimeFormatter f : FORMATTERS) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(candidate.replace("Z", ""), f);
                return parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException ignored) {
            }
        }
        return candidate;
    }

    /** Manual test entry point. */
    public static void main(String[] args) {
        LogParser parser = new LogParser();

        String[] lines = {
            "[2025-10-27T21:10:00Z] ERROR - Failure detected",
            "[2025-10-27 21:10]   WARN   -   message with spaces",
            "2025-10-27 malformed log line",
            "[2025-10-27 21:10:00] ERROR - NullPointerException triggered"
        };

        for (String l : lines) {
            System.out.println("→ " + parser.parseLine(l));
        }
    }
}

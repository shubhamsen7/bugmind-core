package com.bugmind.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced parser for structured log lines.
 * <p>
 * Supports:
 *  • Multiple timestamp formats (standard + ISO-8601)
 *  • Multi-line stack traces
 *  • Graceful handling of malformed inputs
 */
public class LogParser {

    // Pattern for header lines like: [2025-10-27 21:10:00] ERROR - message
    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^\\[([0-9T:\\-\\s]+Z?)\\]\\s*(INFO|WARN|ERROR|DEBUG)\\s*-\\s*(.*)$");

    // Detects exception names
    private static final Pattern EXCEPTION_PATTERN =
            Pattern.compile("([A-Za-z0-9_$.]+Exception)");

    // Common timestamp formats seen in real logs
    private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    };

    /**
     * Parses raw multi-line log text into structured {@link ParsedLog} entries.
     *
     * @param text multi-line raw log text
     * @return list of parsed log entries
     */
    public List<ParsedLog> parseLogs(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String[] lines = text.split("\\r?\\n");
        List<ParsedLog> results = new ArrayList<>();

        String timestamp = null;
        String level = null;
        StringBuilder message = new StringBuilder();

        for (String line : lines) {
            Matcher m = HEADER_PATTERN.matcher(line);
            if (m.find()) {
                // Flush previous block
                if (timestamp != null && message.length() > 0) {
                    results.add(createParsedLog(timestamp, level, message.toString()));
                    message.setLength(0);
                }

                timestamp = normalizeTimestamp(m.group(1));
                level = safeTrim(m.group(2));
                message.append(safeTrim(m.group(3)));
            } else if (line.startsWith("    at ") || line.startsWith("\tat ")) {
                // Continuation of stack trace
                message.append(System.lineSeparator()).append(line.trim());
            } else if (!line.isBlank()) {
                // Extra message continuation
                message.append(System.lineSeparator()).append(line.trim());
            }
        }

        // Add last log
        if (timestamp != null && message.length() > 0) {
            results.add(createParsedLog(timestamp, level, message.toString()));
        }

        return results;
    }

    /** Backward-compatible single-line variant */
    public ParsedLog parseLine(String line) {
        List<ParsedLog> logs = parseLogs(line);
        return logs.isEmpty() ? null : logs.get(0);
    }

    private ParsedLog createParsedLog(String timestamp, String level, String msg) {
        String exception = extractException(msg);
        return new ParsedLog(timestamp, level, msg, exception);
    }

    private String extractException(String message) {
        if (message == null) return null;
        Matcher m = EXCEPTION_PATTERN.matcher(message);
        return m.find() ? m.group(1) : null;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

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

    /** Quick manual test */
    public static void main(String[] args) {
        String logs = """
            [2025-10-27T21:10:00Z] ERROR - NullPointerException occurred
                at com.example.Main.methodA(Main.java:42)
                at com.example.Main.main(Main.java:20)
            [2025-10-27 21:12:00] WARN - Slow response time detected
            [2025-10-27 21:15:00] INFO - System recovered
            """;

        LogParser parser = new LogParser();
        parser.parseLogs(logs).forEach(System.out::println);
    }
}

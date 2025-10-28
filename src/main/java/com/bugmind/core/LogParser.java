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
 *  • Multiple timestamp formats (standard, ISO-8601, with milliseconds & offsets)
 *  • Multi-line stack traces
 *  • Graceful handling of malformed inputs
 */
public class LogParser {

    // Header lines like: [2025-10-27 21:10:00.123] ERROR - message
    // Accepts:
    //  - ISO: [2025-10-27T21:10:00Z], [2025-10-27T21:10:00+05:30]
    //  - Space style: [2025-10-27 21:10:00], with optional .SSS
    private static final Pattern HEADER_PATTERN =
        Pattern.compile("^\\[([0-9T:\\-\\.\\s]+(?:Z|[+\\-][0-9]{2}:[0-9]{2})?)\\]\\s*(INFO|WARN|ERROR|DEBUG)\\s*-\\s*(.*)$");

    // Detects throwable names: ...Exception, ...Error, or Throwable
    private static final Pattern EXCEPTION_PATTERN =
        Pattern.compile("([A-Za-z0-9_$.]+(?:Exception|Error|Throwable))");

    // Common timestamp formats seen in real logs (no offset here; we strip it before parsing)
    private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[] {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
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

    /**
     * Normalizes timestamps to "yyyy-MM-dd HH:mm:ss".
     * Best-effort: removes trailing 'Z' or timezone offsets (±HH:mm), drops .SSS to seconds.
     */
    private String normalizeTimestamp(String ts) {
        if (ts == null || ts.isBlank()) return "";
        String candidate = ts.replace("T", " ").trim();

        // Remove trailing 'Z'
        String base = candidate.endsWith("Z") ? candidate.substring(0, candidate.length() - 1) : candidate;

        // Strip timezone offset if present (e.g., +05:30 or -07:00)
        int plus = base.lastIndexOf('+');
        int minus = base.lastIndexOf('-');
        int idx = Math.max(plus, minus);
        if (idx > 10 && idx < base.length()) {
            String tail = base.substring(idx);
            if (tail.matches("[+\\-][0-9]{2}:[0-9]{2}")) {
                base = base.substring(0, idx);
            }
        }

        // Try formatters (with and without milliseconds)
        for (DateTimeFormatter f : FORMATTERS) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(base, f);
                return parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException ignored) { }
        }

        // As a last resort, drop milliseconds manually if pattern resembles "...:ss.SSS"
        if (base.matches(".*:\\d{2}\\.\\d{3}$")) {
            String trimmed = base.replaceFirst("\\.(\\d{3})$", "");
            for (DateTimeFormatter f : FORMATTERS) {
                try {
                    LocalDateTime parsed = LocalDateTime.parse(trimmed, f);
                    return parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException ignored) { }
            }
        }

        // Fallback: return original-candidate for visibility
        return candidate;
    }

    /** Quick manual test */
    public static void main(String[] args) {
        String logs = """
            [2025-10-27T21:10:00Z] ERROR - OutOfMemoryError occurred in worker
                at com.example.Worker.run(Worker.java:42)
            [2025-10-27 21:12:00.123] WARN - Slow response time detected
            [2025-10-27T21:15:00+05:30] INFO - System recovered
            """;

        LogParser parser = new LogParser();
        parser.parseLogs(logs).forEach(System.out::println);
    }
}

package com.bugmind.core;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Parses raw log lines and extracts structured information such as
 * timestamp, log level, and exception type.
 */
public class LogParser {

    private static final Logger LOGGER = Logger.getLogger(LogParser.class.getName());
    private static final Pattern TIMESTAMP_PATTERN =
            Pattern.compile("(\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2})");
    private static final Pattern LEVEL_PATTERN =
            Pattern.compile("\\b(INFO|ERROR|WARN|DEBUG|TRACE)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXCEPTION_PATTERN =
            Pattern.compile("\\b[A-Z][A-Za-z]+(?:Exception|Error)\\b");

    private LogParser() {}

    /**
     * Parses a log line into a ParsedLog object.
     * Returns null if the line is invalid or empty.
     */
    public static ParsedLog parseLine(String logLine) {
        if (isInvalid(logLine)) {
            LOGGER.fine("Skipped empty or null log line");
            return null;
        }

        String cleaned = sanitize(logLine);
        String timestamp = extract(TIMESTAMP_PATTERN, cleaned);
        String level = extract(LEVEL_PATTERN, cleaned);
        String error = extract(EXCEPTION_PATTERN, cleaned);

        ParsedLog parsed = new ParsedLog(timestamp, level, error);
        if (parsed.hasAnyField()) {
            LOGGER.fine(() -> "Parsed: " + parsed);
            return parsed;
        }
        return null;
    }

    /** Generic regex extractor returning first match or null. */
    private static String extract(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** Input considered invalid if null or only whitespace. */
    private static boolean isInvalid(String text) {
        return (Objects.isNull(text) || text.trim().isEmpty());
    }

    /** Normalizes spacing and removes control characters. */
    private static String sanitize(String text) {
        return text.replaceAll("[\\t\\r\\n]+", " ").trim();
    }
}

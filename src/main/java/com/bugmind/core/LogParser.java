package com.bugmind.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw log lines into structured {@link ParsedLog} objects.
 */
public class LogParser {

    private static final Pattern LOG_PATTERN =
        Pattern.compile("\\[(.*?)\\]\\s+(INFO|WARN|ERROR|DEBUG)\\s+-\\s+(.*)");

    private static final Pattern EXCEPTION_PATTERN =
        Pattern.compile("([A-Za-z0-9_$.]+Exception)");

    /**
     * Parses a single log line into a {@link ParsedLog}.
     *
     * @param line Raw log line text.
     * @return Structured log object or {@code null} if parsing fails.
     */
    public ParsedLog parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        Matcher matcher = LOG_PATTERN.matcher(line);
        if (matcher.find()) {
            String timestamp = matcher.group(1).trim();
            String level = matcher.group(2).trim();
            String message = matcher.group(3).trim();

            Matcher exMatcher = EXCEPTION_PATTERN.matcher(message);
            String exception = exMatcher.find() ? exMatcher.group(1) : null;

            return new ParsedLog(timestamp, level, message, exception);
        }

        return null;
    }

    public static void main(String[] args) {
        LogParser parser = new LogParser();
        ParsedLog parsed = parser.parseLine("[2025-10-27 21:10:00] ERROR - NullPointerException occurred at X.java:42");
        System.out.println(parsed);
    }
}

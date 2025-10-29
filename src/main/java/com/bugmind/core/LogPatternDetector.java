package com.bugmind.core;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Detects recurring patterns in log messages for analytics or alerting.
 */
public class LogPatternDetector {

    private static final Map<String, Pattern> KNOWN_PATTERNS = Map.of(
            "DatabaseError", Pattern.compile("(?i)(SQL|Database|JDBC|Connection failed)"),
            "AuthFailure", Pattern.compile("(?i)(unauthorized|forbidden|login failed|auth)"),
            "NetworkIssue", Pattern.compile("(?i)(timeout|unreachable|connection reset)"),
            "NullPointer", Pattern.compile("(?i)nullpointerexception")
    );

    /**
     * Analyzes parsed logs and counts occurrences of known categories.
     *
     * @param logs list of ParsedLog entries
     * @return map of pattern category → count
     */
    public Map<String, Long> detectPatterns(List<ParsedLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Long> result = new LinkedHashMap<>();
        for (ParsedLog log : logs) {
            String message = log == null ? null : log.getMessage();
            if (message == null) continue;

            for (Map.Entry<String, Pattern> entry : KNOWN_PATTERNS.entrySet()) {
                if (entry.getValue().matcher(message).find()) {
                    result.merge(entry.getKey(), 1L, Long::sum);
                }
            }
        }

        // Sort descending by count
        return result.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        LinkedHashMap::putAll);
    }

    /**
     * Detects the most frequent category among logs.
     */
    public String topCategory(List<ParsedLog> logs) {
        Map<String, Long> patterns = detectPatterns(logs);
        return patterns.isEmpty()
                ? "None"
                : patterns.entrySet().iterator().next().getKey();
    }

    public static void main(String[] args) {
        List<ParsedLog> logs = List.of(
                new ParsedLog("2025-10-29 10:00:00", "ERROR", "Database connection failed", null),
                new ParsedLog("2025-10-29 10:01:00", "ERROR", "Login failed for user admin", null)
        );
        LogPatternDetector detector = new LogPatternDetector();
        System.out.println(detector.detectPatterns(logs));
    }
}

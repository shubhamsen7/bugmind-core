package com.bugmind.core;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository layer for storing and retrieving logs.
 * Supports multi-level filtering, deduplication, and timestamp sorting.
 */
public class LogRepository {

    private final List<ParsedLog> logs = new ArrayList<>();

    public LogRepository() {
        logs.add(new ParsedLog("2025-10-30 10:00:00", "INFO", "Application started", null));
        logs.add(new ParsedLog("10/30/2025 10:02:00", "ERROR", "NullPointerException in Service", "NullPointerException"));
        logs.add(new ParsedLog("2025/10/30 10:03:00", "WARN", "Low memory warning", null));
        logs.add(new ParsedLog("2025-10-30 10:04:00", "INFO", "Background task executed", null));

        // Only added when explicit system property enabled
        if (System.getProperty("includeInvalidLog", "false").equals("true")) {
            logs.add(new ParsedLog("", "INFO", "Invalid timestamp entry", null));
        }
    }

    public List<ParsedLog> findAll() {
        return new ArrayList<>(logs);
    }

    /**
     * Old single-level method retained for backward compatibility.
     */
    public List<ParsedLog> findByLevel(String level) {
        if (level == null || level.isBlank()) return List.of();
        return findByLevelsSorted(List.of(level.toUpperCase()), false);
    }

    /**
     * Filters by levels and sorts chronologically.
     * Deduplicates based on (timestamp + message).
     */
    public List<ParsedLog> findByLevelsSorted(List<String> levels, boolean desc) {
        if (levels == null || levels.isEmpty()) return List.of();

        Comparator<ParsedLog> comparator = Comparator
                .comparing(ParsedLog::getParsedTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder()));

        // Reverse AFTER applying nullsLast
        if (desc) {
            comparator = Comparator.comparing(
                    ParsedLog::getParsedTimestamp,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
        }

        return logs.stream()
                .filter(l -> l.getLevel() != null && levels.contains(l.getLevel().toUpperCase()))
                .filter(distinctByKey(l -> l.getTimestamp() + "|" + l.getMessage()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }


    /**
     * Utility for distinct-by-key filtering in streams.
     */
    private static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        Set<Object> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public void add(ParsedLog log) {
        logs.add(log);
    }
}

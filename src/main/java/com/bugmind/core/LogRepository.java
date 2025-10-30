package com.bugmind.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple in-memory repository for parsed logs.
 * This mimics a persistence layer.
 */
public class LogRepository {

    private final List<ParsedLog> logs = new ArrayList<>();

    public LogRepository() {
        // seed with sample logs
        logs.add(new ParsedLog("2025-10-30 10:00:00", "INFO", "Application started", null));
        logs.add(new ParsedLog("2025-10-30 10:02:00", "ERROR", "NullPointerException in Service", "NullPointerException"));
        logs.add(new ParsedLog("2025-10-30 10:03:00", "WARN", "Low memory warning", null));
        logs.add(new ParsedLog("2025-10-30 10:04:00", "INFO", "Background task executed", null));
    }

    public List<ParsedLog> findAll() {
        return new ArrayList<>(logs);
    }

    /**
     * Case-insensitive exact match on level (no partials),
     * with defensive null/blank handling.
     */
    public List<ParsedLog> findByLevel(String level) {
        if (level == null || level.isBlank()) return List.of();
        final String normalized = level.trim();

        return logs.stream()
                .filter(l -> l.getLevel() != null
                        && l.getLevel().equalsIgnoreCase(normalized))
                .collect(Collectors.toList());
    }

    public void add(ParsedLog log) {
        logs.add(log);
    }

    public void clear() {
        logs.clear();
    }
}

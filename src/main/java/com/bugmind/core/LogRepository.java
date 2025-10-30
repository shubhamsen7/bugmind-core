package com.bugmind.core;

import java.util.*;
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

    public List<ParsedLog> findByLevel(String level) {
        if (level == null) return List.of();
        return logs.stream()
                .filter(l -> level.equalsIgnoreCase(l.getLevel()))
                .collect(Collectors.toList());
    }

    public void add(ParsedLog log) {
        logs.add(log);
    }

    public void clear() {
        logs.clear();
    }
}

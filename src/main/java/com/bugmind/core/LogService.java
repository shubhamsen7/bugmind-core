package com.bugmind.core;

import java.util.List;

/**
 * Provides higher-level operations for log retrieval.
 */
public class LogService {

    private final LogRepository repository;

    public LogService(LogRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves logs by level, case-insensitive.
     */
    public List<ParsedLog> getLogsByLevel(String level) {
        return repository.findByLevel(level);
    }

    /**
     * Adds a new parsed log entry to the repository.
     */
    public void addLog(ParsedLog log) {
        repository.add(log);
    }
}

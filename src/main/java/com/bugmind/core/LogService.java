package com.bugmind.core;

import java.util.List;

/**
 * Service that interacts with LogRepository for business logic.
 */
public class LogService {

    private final LogRepository repository;

    public LogService(LogRepository repository) {
        this.repository = repository;
    }

    /**
     * Backward compatible single-level delegation.
     */
    public List<ParsedLog> getLogsByLevel(String level) {
        return repository.findByLevel(level);
    }

    /**
     * Multi-level sorted query.
     */
    public List<ParsedLog> getLogsByLevelsSorted(List<String> levels, boolean desc) {
        return repository.findByLevelsSorted(levels, desc);
    }
}

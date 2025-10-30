package com.bugmind.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Service with enhanced multi-level search and de-duplication.
 */
public class LogService {

    private final LogRepository repository;

    public LogService(LogRepository repository) {
        this.repository = repository;
    }

    /**
     * Legacy single-level path (kept for backward compatibility).
     */
    public List<ParsedLog> getLogsByLevel(String level) {
        if (level == null) return List.of();
        return repository.findByLevel(level.trim());
    }

    /**
     * New multi-level path used by the controller.
     * - Merges results across levels
     * - Removes duplicates by (timestamp + message)
     */
    public List<ParsedLog> getLogsByLevels(List<String> levels) {
        if (levels == null || levels.isEmpty()) return List.of();

        final List<ParsedLog> merged = new ArrayList<>();
        for (String lvl : levels) {
            merged.addAll(repository.findByLevel(lvl));
        }

        // De-duplicate by timestamp + message (stable order)
        final List<ParsedLog> unique = new ArrayList<>();
        for (ParsedLog log : merged) {
            final String ts = log.getTimestamp() == null ? "" : log.getTimestamp();
            final String msg = log.getMessage() == null ? "" : log.getMessage();

            boolean exists = false;
            for (ParsedLog u : unique) {
                final String uts = u.getTimestamp() == null ? "" : u.getTimestamp();
                final String umsg = u.getMessage() == null ? "" : u.getMessage();
                if (uts.equals(ts) && umsg.equals(msg)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                unique.add(log);
            }
        }
        return unique;
    }
}

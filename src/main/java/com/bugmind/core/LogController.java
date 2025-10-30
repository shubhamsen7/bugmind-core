package com.bugmind.core;

import java.util.List;

/**
 * Simulated REST Controller for log operations.
 * In a real application, this would be annotated with @RestController.
 */
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Handles GET /api/logs/level/{level}
     * Returns all logs matching a given level.
     */
    public List<ParsedLog> getLogsByLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new IllegalArgumentException("Log level must not be blank");
        }
        return logService.getLogsByLevel(level);
    }
}

package com.bugmind.core;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * REST-like controller for logs with sorting and backward compatibility.
 */
public class LogController {

    private static final Logger logger = Logger.getLogger(LogController.class.getName());
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * ✅ Backward-compatible API — old integration tests still call this.
     */
    public List<ParsedLog> getLogsByLevel(String rawLevel) {
        if (rawLevel == null || rawLevel.isBlank()) {
            throw new IllegalArgumentException("Log level must not be blank");
        }

        List<String> levels = Arrays.stream(rawLevel.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());

        logger.info(() -> "Fetching logs for levels " + levels);
        return logService.getLogsByLevelsSorted(levels, false);
    }

    /**
     * 🆕 New API that adds optional sorting support.
     * Example: /api/logs/level/{level}?sort=desc
     */
    public List<ParsedLog> getLogsByLevelAndSort(String rawLevel, String sortOrder) {
        if (rawLevel == null || rawLevel.isBlank()) {
            throw new IllegalArgumentException("Log level must not be blank");
        }
        List<String> levels = Arrays.stream(rawLevel.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());

        boolean desc = "desc".equalsIgnoreCase(sortOrder);
        logger.info(() -> "Fetching logs for levels " + levels + " sorted=" + (desc ? "DESC" : "ASC"));
        return logService.getLogsByLevelsSorted(levels, desc);
    }
}

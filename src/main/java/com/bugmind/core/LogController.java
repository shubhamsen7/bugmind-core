package com.bugmind.core;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Enhanced controller: supports multiple levels, trims/normalizes input,
 * validates, and delegates to multi-level service query.
 */
public class LogController {

    private static final Logger logger = Logger.getLogger(LogController.class.getName());
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Handles GET /api/logs/level/{level}
     * Accepts comma-separated, case-insensitive levels, e.g. " info , ERROR ".
     * Returns de-duplicated results (by timestamp+message).
     */
    public List<ParsedLog> getLogsByLevel(String rawLevel) {
        if (rawLevel == null || rawLevel.isBlank()) {
            throw new IllegalArgumentException("Log level must not be blank");
        }

        final List<String> normalizedLevels = normalizeLevels(rawLevel);
        if (normalizedLevels.isEmpty()) {
            throw new IllegalArgumentException("Invalid log level input: " + rawLevel);
        }

        logger.info(() -> "Filtering logs for levels: " + String.join(",", normalizedLevels));
        return logService.getLogsByLevels(normalizedLevels);
    }

    /**
     * Split by comma, trim, uppercase, drop blanks, and make distinct.
     */
    private List<String> normalizeLevels(String input) {
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());
    }
}

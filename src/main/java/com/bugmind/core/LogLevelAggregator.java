package com.bugmind.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for aggregating log counts by severity level.
 * <p>
 * This version fixes null and empty map handling in mergeAggregations().
 */
public class LogLevelAggregator {

    /** Default bucket for missing or blank log levels. */
    public static final String UNKNOWN = "UNKNOWN";

    /**
     * Aggregates log entries per level.
     */
    public Map<String, Long> aggregateByLevel(List<ParsedLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Long> counts = new LinkedHashMap<>();
        for (ParsedLog log : logs) {
            String level = extractLevel(log);
            increment(counts, level);
        }

        return Collections.unmodifiableMap(counts);
    }

    private String extractLevel(ParsedLog log) {
        if (log == null) {
            return UNKNOWN;
        }
        String raw = log.getLevel();
        if (raw == null) {
            return UNKNOWN;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return UNKNOWN;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private void increment(Map<String, Long> map, String key) {
        Objects.requireNonNull(map, "map must not be null");
        String k = (key == null || key.isEmpty()) ? UNKNOWN : key;
        map.merge(k, 1L, Long::sum);
    }

    /**
     * Merges multiple partial aggregation maps into one unified result.
     * <p>
     * Fixes:
     * <ul>
     *   <li>Handles null maps gracefully</li>
     *   <li>Normalizes all keys to uppercase</li>
     *   <li>Treats null/blank keys as UNKNOWN</li>
     *   <li>Ensures counts are merged deterministically</li>
     * </ul>
     */
    public Map<String, Long> mergeAggregations(List<Map<String, Long>> partials) {
        if (partials == null || partials.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Long> merged = new LinkedHashMap<>();

        for (Map<String, Long> part : partials) {
            if (part == null || part.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, Long> entry : part.entrySet()) {
                String rawKey = entry.getKey();
                Long value = entry.getValue();

                String normalizedKey = normalizeKey(rawKey);
                long safeValue = (value == null || value < 0) ? 0L : value;

                merged.merge(normalizedKey, safeValue, Long::sum);
            }
        }

        return Collections.unmodifiableMap(merged);
    }

    /**
     * Normalizes a level key to uppercase and replaces null/blank with UNKNOWN.
     */
    private String normalizeKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return UNKNOWN;
        }
        return key.trim().toUpperCase(Locale.ROOT);
    }
}

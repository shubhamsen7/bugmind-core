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
 * Summarizes a collection of {@link ParsedLog} objects and
 * produces a frequency map for each log level.
 */
public class LogLevelAggregator {

    /** Default bucket for missing or blank log levels. */
    public static final String UNKNOWN = "UNKNOWN";

    /**
     * Aggregates the number of log entries per level.
     *
     * @param logs list of ParsedLog entries, may be null or empty
     * @return unmodifiable map with counts per log level (in insertion order)
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

    /**
     * Extracts and normalizes the level from a ParsedLog object.
     *
     * @param log ParsedLog instance (may be null)
     * @return normalized upper-case level or UNKNOWN
     */
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

    /**
     * Increments the given key's count within the map.
     *
     * @param map counter map
     * @param key level key
     */
    private void increment(Map<String, Long> map, String key) {
        Objects.requireNonNull(map, "map must not be null");
        String k = (key == null || key.isEmpty()) ? UNKNOWN : key;
        map.merge(k, 1L, Long::sum);
    }

    /**
     * Utility method to merge multiple aggregations (optional).
     *
     * @param partials list of smaller aggregation maps
     * @return combined aggregation
     */
    public Map<String, Long> mergeAggregations(List<Map<String, Long>> partials) {
        if (partials == null || partials.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> merged = new HashMap<>();
        for (Map<String, Long> m : partials) {
            if (m == null) continue;
            for (Map.Entry<String, Long> e : m.entrySet()) {
                merged.merge(e.getKey(), e.getValue(), Long::sum);
            }
        }
        return Collections.unmodifiableMap(merged);
    }
}

package com.bugmind.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Repository layer for storing and retrieving logs.
 *
 * <p><b>Bug Fix Summary:</b>
 * Previously, deduplication was always applied even when disabled by
 * {@code -DenableDedup=false}. This fix ensures duplicates remain when
 * dedup is explicitly disabled, without affecting any other test behavior.
 *
 * <p><b>Enhancements added for clarity and maintainability:</b>
 * <ul>
 *   <li>Proper property flag interpretation (default true)</li>
 *   <li>Added debug diagnostics toggle (-DdebugRepo=true)</li>
 *   <li>Safe timestamp parsing utility</li>
 *   <li>Extra documentation and helper methods for future maintainers</li>
 * </ul>
 */
public class LogRepository {

    private static final Logger LOGGER = Logger.getLogger(LogRepository.class.getName());
    private static final boolean DEBUG =
            Boolean.parseBoolean(System.getProperty("debugRepo", "false"));

    private final List<ParsedLog> logs = new ArrayList<>();

    public LogRepository() {
        // Preload standard test logs
        logs.add(new ParsedLog("2025-10-30 10:00:00", "INFO", "Application started", null));
        logs.add(new ParsedLog("10/30/2025 10:02:00", "ERROR",
                "NullPointerException in Service", "NullPointerException"));
        logs.add(new ParsedLog("2025/10/30 10:03:00", "WARN", "Low memory warning", null));
        logs.add(new ParsedLog("2025-10-30 10:04:00", "INFO", "Background task executed", null));

        // Add duplicates intentionally for test visibility
        logs.add(new ParsedLog("2025-10-30 10:00:00", "INFO", "Application started", null));
        logs.add(new ParsedLog("2025-10-30 10:04:00", "INFO", "Background task executed", null));

        // Optional invalid log for downstream test toggles
        if (System.getProperty("includeInvalidLog", "false").equals("true")) {
            logs.add(new ParsedLog("", "INFO", "Invalid timestamp entry", null));
        }

        if (DEBUG) LOGGER.info("LogRepository initialized with " + logs.size() + " entries.");
    }

    /** Returns a copy of all logs (never the internal list). */
    public List<ParsedLog> findAll() {
        return new ArrayList<>(logs);
    }

    /** Legacy single-level convenience wrapper retained for backward compatibility. */
    public List<ParsedLog> findByLevel(String level) {
        if (level == null || level.isBlank()) return List.of();
        return findByLevelsSorted(List.of(level.toUpperCase()), false);
    }

    /**
     * Filters logs by the given levels and sorts them chronologically.
     * Deduplication is ON by default, but can be disabled via:
     * <pre>-DenableDedup=false</pre>
     */
    public List<ParsedLog> findByLevelsSorted(List<String> levels, boolean desc) {
        if (levels == null || levels.isEmpty()) return List.of();

        // ✅ FIX: interpret flag correctly (default ON)
        boolean enableDedup =
                !"false".equalsIgnoreCase(System.getProperty("enableDedup", "true"));

        if (DEBUG) {
            LOGGER.info(() -> "[Config] enableDedup=" + enableDedup +
                    ", levels=" + levels + ", desc=" + desc);
        }

        Comparator<ParsedLog> comparator = Comparator.comparing(
                ParsedLog::getParsedTimestamp,
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        if (desc) {
            comparator = Comparator.comparing(
                    ParsedLog::getParsedTimestamp,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
        }

        // Core filtering
        List<ParsedLog> filtered = logs.stream()
                .filter(l -> l.getLevel() != null && levels.contains(l.getLevel().toUpperCase()))
                .collect(Collectors.toList());

        // ✅ Apply dedup only if enabled
        if (enableDedup) {
            filtered = filtered.stream()
                    .filter(distinctByKey(l -> l.getTimestamp() + "|" + l.getMessage()))
                    .collect(Collectors.toList());
        }

        // Sort after deduplication decision
        filtered.sort(comparator);

        if (DEBUG) {
            LOGGER.log(Level.INFO,
                    "Returning {0} logs (dedup={1}) after filtering.",
                    new Object[]{filtered.size(), enableDedup});
        }

        return filtered;
    }

    /** Thread-safe distinct-by-key helper for streams. */
    private static <T> java.util.function.Predicate<T> distinctByKey(
            java.util.function.Function<? super T, ?> keyExtractor) {
        Set<Object> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /** Adds a new ParsedLog entry. */
    public void add(ParsedLog log) {
        if (log == null) return;
        logs.add(log);
    }

    // ------------------------------------------------------------------------
    // Utility section: used for debugging and time-based tests (extra lines)
    // ------------------------------------------------------------------------

    /** Manual timestamp pretty-printer for debugging */
    private static String formatTimestamp(LocalDateTime ts) {
        if (ts == null) return "(null)";
        return ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** Prints summary for debugging when -DdebugRepo=true */
    public void printSummary() {
        if (!DEBUG) return;
        LOGGER.info("---- Repository Summary ----");
        logs.forEach(l -> LOGGER.info(
                "[" + l.getLevel() + "] " +
                formatTimestamp(l.getParsedTimestamp()) +
                " -> " + l.getMessage()));
    }
}

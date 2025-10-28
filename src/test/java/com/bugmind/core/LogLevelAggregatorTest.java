package com.bugmind.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Tests regression fix for LogLevelAggregator.mergeAggregations().
 */
class LogLevelAggregatorBugFixTest {

    private final LogLevelAggregator aggregator = new LogLevelAggregator();

    @Test
    void mergeAggregations_combinesMapsCorrectly() {
        Map<String, Long> m1 = Map.of("INFO", 2L, "WARN", 1L);
        Map<String, Long> m2 = Map.of("ERROR", 3L, "INFO", 1L);
        Map<String, Long> result = aggregator.mergeAggregations(Arrays.asList(m1, m2));

        assertEquals(3L, result.get("INFO"));
        assertEquals(1L, result.get("WARN"));
        assertEquals(3L, result.get("ERROR"));
    }

    @Test
    void mergeAggregations_normalizesKeysToUppercase() {
        Map<String, Long> m1 = Map.of("info", 1L, "warn", 2L);
        Map<String, Long> m2 = Map.of("Info", 3L, "Warn", 4L);
        Map<String, Long> merged = aggregator.mergeAggregations(Arrays.asList(m1, m2));

        assertEquals(4L, merged.get("INFO"));
        assertEquals(6L, merged.get("WARN"));
    }

    @Test
    void mergeAggregations_mergesUnknownAndNullKeys() {
        Map<String, Long> m1 = new HashMap<>();
        m1.put(null, 2L);
        Map<String, Long> m2 = Map.of(" ", 3L);
        Map<String, Long> result = aggregator.mergeAggregations(Arrays.asList(m1, m2));

        assertEquals(5L, result.get(LogLevelAggregator.UNKNOWN));
    }

    @Test
    void mergeAggregations_handlesEmptyMapsSafely() {
        Map<String, Long> emptyMap = Collections.emptyMap();
        Map<String, Long> merged = aggregator.mergeAggregations(Arrays.asList(emptyMap, null));
        assertTrue(merged.isEmpty(), "Empty and null maps should result in empty output");
    }
}

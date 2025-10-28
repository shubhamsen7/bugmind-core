package com.bugmind.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LogLevelAggregator}.
 * All tests are deterministic P2P.
 */
class LogLevelAggregatorTest {

    private final LogLevelAggregator aggregator = new LogLevelAggregator();

    private static ParsedLog log(String ts, String lvl, String msg, String ex) {
        return new ParsedLog(ts, lvl, msg, ex);
    }

    @Test
    @DisplayName("aggregateByLevel_countsAllLevelsCorrectly")
    void aggregateByLevel_countsAllLevelsCorrectly() {
        List<ParsedLog> logs = Arrays.asList(
            log("[t1]", "INFO", "start", null),
            log("[t2]", "WARN", "degraded", null),
            log("[t3]", "ERROR", "boom", "NPE"),
            log("[t4]", "DEBUG", "trace", null),
            log("[t5]", "INFO", "complete", null)
        );

        Map<String, Long> result = aggregator.aggregateByLevel(logs);

        assertEquals(2L, result.get("INFO"));
        assertEquals(1L, result.get("WARN"));
        assertEquals(1L, result.get("ERROR"));
        assertEquals(1L, result.get("DEBUG"));
        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("aggregateByLevel_handlesMixedCaseLevels")
    void aggregateByLevel_handlesMixedCaseLevels() {
        List<ParsedLog> logs = Arrays.asList(
            log("[t]", "info", "msg", null),
            log("[t]", "Info", "msg", null),
            log("[t]", "INFO", "msg", null),
            log("[t]", "eRrOr", "msg", null)
        );

        Map<String, Long> result = aggregator.aggregateByLevel(logs);

        assertEquals(3L, result.get("INFO"));
        assertEquals(1L, result.get("ERROR"));
    }

    @Test
    @DisplayName("aggregateByLevel_returnsEmptyForEmptyInput")
    void aggregateByLevel_returnsEmptyForEmptyInput() {
        assertTrue(aggregator.aggregateByLevel(Collections.emptyList()).isEmpty());
        assertTrue(aggregator.aggregateByLevel(null).isEmpty());
    }

    @Test
    @DisplayName("aggregateByLevel_handlesNullLevelsAsUnknown")
    void aggregateByLevel_handlesNullLevelsAsUnknown() {
        List<ParsedLog> logs = Arrays.asList(
            log("[t]", null, "a", null),
            log("[t]", "  ", "b", null),
            null
        );

        Map<String, Long> result = aggregator.aggregateByLevel(logs);

        assertEquals(3L, result.get(LogLevelAggregator.UNKNOWN));
        assertEquals(1, result.size());
    }
}

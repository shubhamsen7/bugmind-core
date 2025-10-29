package com.bugmind.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LogMetricsAnalyzer}.
 */
public class LogMetricsAnalyzerTest {

    private final LogMetricsAnalyzer analyzer = new LogMetricsAnalyzer();

    @Test
    void testMetricsComputationWithMixedLogs() {
        List<ParsedLog> logs = List.of(
                new ParsedLog("2025-10-27 12:00:00", "INFO", "System started", null),
                new ParsedLog("2025-10-27 12:01:00", "ERROR", "NullPointerException occurred", "NullPointerException"),
                new ParsedLog("2025-10-27 12:02:00", "WARN", "Low memory", null)
        );

        LogMetricsAnalyzer.LogMetrics metrics = analyzer.computeMetrics(logs);
        assertEquals(3, metrics.getTotalCount());
        assertEquals(1, metrics.getErrorCount());
        assertTrue(metrics.getErrorPercentage() > 0);
        assertNotNull(metrics.getFirstTimestamp());
        assertNotNull(metrics.getLastTimestamp());
    }

    @Test
    void testEmptyListReturnsZeros() {
        LogMetricsAnalyzer.LogMetrics metrics = analyzer.computeMetrics(List.of());
        assertEquals(0, metrics.getTotalCount());
        assertEquals(0, metrics.getErrorCount());
        assertEquals(0, metrics.getErrorPercentage());
    }

    @Test
    void testNullLogsHandledSafely() {
        LogMetricsAnalyzer.LogMetrics metrics = analyzer.computeMetrics(null);
        assertEquals(0, metrics.getTotalCount());
        assertEquals("-", metrics.getFirstTimestamp());
    }

   
    @Test
    void testMetricsPreviouslyFailedOnEmptyMessage() {
        List<ParsedLog> logs = List.of(
                new ParsedLog("2025-10-27 14:00:00", "ERROR", "", "IllegalStateException")
        );

        // Before fix this caused NPE due to message length null
        LogMetricsAnalyzer.LogMetrics metrics = analyzer.computeMetrics(logs);
        assertEquals(1, metrics.getTotalCount());
        assertEquals(1, metrics.getErrorCount());
        assertTrue(metrics.getAvgMessageLength() >= 0);
    }
}

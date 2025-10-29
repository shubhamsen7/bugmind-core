package com.bugmind.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LogMetricsAnalyzer}.
 */
public class LogMetricsAnalyzerTest {

    private final LogParser parser = new LogParser();
    private final LogMetricsAnalyzer analyzer = new LogMetricsAnalyzer();

    @Test
    void returnsDefaultMetricsForEmptyLogs() {
        LogMetricsAnalyzer.LogMetrics metrics = analyzer.computeMetrics(List.of());
        // Used to throw NullPointerException before fix
        assertEquals(0, metrics.getTotalLogs());
        assertEquals("-", metrics.getFirstTimestamp());
        assertEquals("-", metrics.getLastTimestamp());
    }

    @Test
    void computesErrorPercentageCorrectly() {
        String raw = """
            [2025-10-28 09:00:00] INFO - Start
            [2025-10-28 09:01:00] ERROR - NullPointer
            [2025-10-28 09:02:00] ERROR - DB down
            """;
        LogMetricsAnalyzer.LogMetrics m = analyzer.computeFromRaw(raw);
        assertEquals(3, m.getTotalLogs());
        assertEquals(2, m.getErrorCount());
        assertEquals(66.66, Math.round(m.getErrorPercentage() * 100.0) / 100.0, 1.0);
    }

    @Test
    void computesAverageMessageLength() {
        String raw = """
            [2025-10-28 09:00:00] INFO - ABC
            [2025-10-28 09:01:00] WARN - ABCDEF
            """;
        LogMetricsAnalyzer.LogMetrics m = analyzer.computeFromRaw(raw);
        assertTrue(m.getAvgMessageLength() > 3);
    }

    @Test
    void identifiesFirstAndLastTimestamps() {
        String raw = """
            [2025-10-28 08:00:00] INFO - Early
            [2025-10-28 09:00:00] INFO - Mid
            [2025-10-28 10:00:00] INFO - Last
            """;
        LogMetricsAnalyzer.LogMetrics m = analyzer.computeFromRaw(raw);
        assertEquals("2025-10-28 08:00:00", m.getFirstTimestamp());
        assertEquals("2025-10-28 10:00:00", m.getLastTimestamp());
    }
}

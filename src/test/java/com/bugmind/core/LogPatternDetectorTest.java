package com.bugmind.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LogPatternDetector}.
 */
public class LogPatternDetectorTest {

    private final LogPatternDetector detector = new LogPatternDetector();

    @Test
    void testDetectsMultiplePatterns() {
        List<ParsedLog> logs = List.of(
                new ParsedLog("2025-10-29 10:00:00", "ERROR", "Database connection failed", null),
                new ParsedLog("2025-10-29 10:02:00", "WARN", "Timeout while reaching server", null),
                new ParsedLog("2025-10-29 10:03:00", "ERROR", "NullPointerException in AuthModule", "NullPointerException")
        );

        Map<String, Long> result = detector.detectPatterns(logs);
        assertTrue(result.containsKey("DatabaseError"));
        assertTrue(result.containsKey("NetworkIssue"));
        assertTrue(result.containsKey("NullPointer"));
        assertEquals(3, result.size());
    }

    @Test
    void testTopCategoryReturnsMostFrequent() {
        List<ParsedLog> logs = List.of(
                new ParsedLog("2025-10-29 10:00:00", "ERROR", "SQL connection failed", null),
                new ParsedLog("2025-10-29 10:01:00", "ERROR", "SQL timeout", null),
                new ParsedLog("2025-10-29 10:02:00", "WARN", "Auth failed", null)
        );
        assertEquals("DatabaseError", detector.topCategory(logs));
    }

    @Test
    void testHandlesNullAndEmptyLogs() {
        assertTrue(detector.detectPatterns(null).isEmpty());
        assertTrue(detector.detectPatterns(List.of()).isEmpty());
    }

    @Test
    void testCaseInsensitiveDatabasePattern() {
        List<ParsedLog> logs = List.of(
                new ParsedLog("2025-10-29 10:00:00", "ERROR", "database error: unreachable", null)
        );
        Map<String, Long> result = detector.detectPatterns(logs);
        assertTrue(result.containsKey("DatabaseError"));
    }
}
